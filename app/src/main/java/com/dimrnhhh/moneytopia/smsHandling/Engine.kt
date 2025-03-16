package com.dimrnhhh.moneytopia.smsHandling

val availableBalanceKeywords = listOf(
    "avbl bal",
    "available balance",
    "available limit",
    "available credit limit",
    "avbl. credit limit",
    "limit available",
    "a/c bal",
    "ac bal",
    "available bal",
    "avl bal",
    "updated balance",
    "total balance",
    "new balance",
    "bal",
    "avl lmt",
    "available"
)

val outstandingBalanceKeywords = listOf("outstanding")

val wallets = listOf("paytm", "simpl", "lazypay", "amazon_pay")

val upiKeywords = listOf("upi", "ref no", "upi ref", "upi ref no")

// Data class to represent combined words
data class CombinedWord(
    val regex: Regex,
    val word: String,
    val type: AccountType
)

val combinedWords = listOf(
    CombinedWord(Regex("credit\\scard"), "c_card", AccountType.CARD),
    CombinedWord(Regex("amazon\\spay"), "amazon_pay", AccountType.WALLET),
    CombinedWord(Regex("uni\\scard"), "uni_card", AccountType.CARD),
    CombinedWord(Regex("niyo\\scard"), "niyo", AccountType.ACCOUNT),
    CombinedWord(Regex("slice\\scard"), "slice_card", AccountType.CARD),
    CombinedWord(Regex("one\\s*card"), "one_card", AccountType.CARD)
)

val upiHandles = listOf(
    "@BARODAMPAY",
    "@rbl",
    "@idbi",
    "@upi",
    "@aubank",
    "@axisbank",
    "@bandhan",
    "@dlb",
    "@indus",
    "@kbl",
    "@federal",
    "@sbi",
    "@uco",
    "@citi",
    "@citigold",
    "@dlb",
    "@dbs",
    "@freecharge",
    "@okhdfcbank",
    "@okaxis",
    "@oksbi",
    "@okicici",
    "@yesg",
    "@hsbc",
    "@idbi",
    "@icici",
    "@indianbank",
    "@allbank",
    "@kotak",
    "@ikwik",
    "@unionbankofindia",
    "@uboi",
    "@unionbank",
    "@paytm",
    "@ybl",
    "@axl",
    "@ibl",
    "@sib",
    "@yespay"
)


enum class AccountType {
    CARD, WALLET, ACCOUNT
}

enum class BalanceKeyWordsType {
    AVAILABLE, OUTSTANDING
}

data class AccountInfo(
    var type: AccountType?,
    var number: String?,
    val name: String?
)

data class Balance(
    val available: String?,
    val outstanding: String?
)

typealias TransactionType = String
typealias MessageType = Any

data class Transaction(
    val type: TransactionType?,
    val amount: String?,
    val referenceNo: String?,
    val merchant: String?
)

data class TransactionInfo(
    val account: AccountInfo,
    val balance: Balance?,
    val transaction: Transaction
)

data class CombinedWords(
    val regex: Regex,
    val word: String,
    val type: AccountType
)

data class TransactionDetails(
    var merchant: String?,
    var referenceNo: String?
)

private fun isNumber(val_: Any): Boolean {
    return try {
        val_.toString().toDouble()
        true
    } catch (e: NumberFormatException) {
        false
    }
}

fun trimLeadingAndTrailingChars(str: String): String {
    val first = str.firstOrNull()
    val last = str.lastOrNull()

    var finalStr = str
    if (!isNumber(last!!)) {
        finalStr = finalStr.dropLast(1)
    }
    if (!isNumber(first!!)) {
        finalStr = finalStr.drop(1)
    }

    return finalStr
}

fun extractBondedAccountNo(accountNo: String): String {
    val strippedAccountNo = accountNo.replace("ac", "")
    return if (isNumber(strippedAccountNo)) strippedAccountNo else ""
}

fun processMessage(message: String): List<String> {
    // Convert to lowercase
    var messageStr = message.lowercase()

    // Perform replacements
    messageStr = messageStr.replace("!", "")
        .replace(":", " ")
        .replace("/", "")
        .replace("=", " ")
        .replace(Regex("[{}]"), " ")
        .replace("\n", " ")
        .replace("\r", " ")
        .replace("ending ", "")
        .replace(Regex("x|[*]"), "")
        .replace("is ", "")
        .replace("with ", "")
        .replace("no. ", "")
        .replace(Regex("\\bac\\b|\\bacct\\b|\\baccount\\b"), "ac")
        .replace(Regex("rs(?=\\w)"), "rs. ")
        .replace("rs ", "rs. ")
        .replace(Regex("inr(?=\\w)"), "rs. ")
        .replace("inr ", "rs. ")
        .replace("rs. ", "rs.")
        .replace(Regex("rs.(?=\\w)"), "rs. ")
        .replace("debited", " debited ")
        .replace("credited", " credited ")

    // Combine words using a predefined list of replacements
    combinedWords.forEach { word ->
        messageStr = messageStr.replace(word.regex, word.word)
    }

    // Split the string and filter out empty strings
    return messageStr.split(" ").filter { it.isNotBlank() }
}

fun getProcessedMessage(message: MessageType): List<String> {
    return when (message) {
        is String -> processMessage(message)
        is List<*> -> message.filterIsInstance<String>()
        else -> throw IllegalArgumentException("Invalid message type")
    }
}

fun padCurrencyValue(val_: String): String {
    val parts = val_.split(".")
    return "${parts[0]}.${parts.getOrNull(1)?.padEnd(2, '0') ?: ""}"
}

fun getNextWords(source: String, searchWord: String, count: Int = 1): String {
    // Split the source string by the searchWord, limiting to 2 parts
    val splits = source.split(searchWord, limit = 2)
    if (splits.size > 1) {
        val nextGroup = splits[1]
        // Split the remaining string using a regex to match non-alphanumeric characters
        val wordSplitRegex = Regex("[^0-9a-zA-Z]+")
        return nextGroup.trim()
            .split(wordSplitRegex)
            .take(count) // Take the required number of words
            .joinToString(" ") // Join the words with a space
    }
    return ""
}


fun extractMerchantInfo(message: MessageType): TransactionDetails {
    val processedMessage = getProcessedMessage(message)
    val messageString = processedMessage.joinToString(" ")
    val transactionDetails = TransactionDetails(merchant = null, referenceNo = null)

    if (processedMessage.contains("vpa")) {
        val idx = processedMessage.indexOf("vpa")
        // If the keyword "vpa" is not the last one
        if (idx < processedMessage.size - 1) {
            val nextStr = processedMessage[idx + 1]
            val name = nextStr.replace(Regex("[()]"), " ").split(" ").firstOrNull()
            transactionDetails.merchant = name
        }
    }

    var match = ""
    for (keyword in upiKeywords) {
        val idx = messageString.indexOf(keyword)
        if (idx > 0) {
            match = keyword
        }
    }

    if (match.isNotEmpty()) {
        val nextWord = getNextWords(messageString, match)
        if (isNumber(nextWord)) {
            transactionDetails.referenceNo = nextWord
        } else if (transactionDetails.merchant != null) {
            val longestNumeric = nextWord
                .split(Regex("[^0-9]"))
                .maxByOrNull { it.length } ?: ""
            if (longestNumeric.isNotEmpty()) {
                transactionDetails.referenceNo = longestNumeric
            }
        } else {
            transactionDetails.merchant = nextWord
        }

        if (transactionDetails.merchant == null) {
            val upiRegex = Regex("[a-zA-Z0-9_-]+(${upiHandles.joinToString("|")})", RegexOption.IGNORE_CASE)
            val matches = upiRegex.findAll(messageString).map { it.value }.toList()
            if (matches.isNotEmpty()) {
                transactionDetails.merchant = matches.firstOrNull()?.split(" ")?.lastOrNull()
            }
        }
    }

    return transactionDetails
}


fun extractPaidTo(message: String): String {
    // Define patterns for identifying "paid to" information
    val patterns = listOf(
        Regex("trf to ([A-Za-z ]+)", RegexOption.IGNORE_CASE), // Matches "trf to <name>"
        Regex("used at ([A-Za-z ]+) for", RegexOption.IGNORE_CASE), // Matches "used at <name> for"
        Regex("on ([A-Za-z ]+) Avl Limit", RegexOption.IGNORE_CASE), // Matches "on <name>. Avl Limit"
        Regex("at ([^,]+),", RegexOption.IGNORE_CASE) // Matches "at <name>,"
    )

    // Try each pattern and return the first match
    for (pattern in patterns) {
        val matchResult = pattern.find(message)
        if (matchResult != null) {
            val paidTo = matchResult.groupValues[1].trim()
            return paidTo
        }
    }

    return ""
}

fun getTransactionAmount(message: MessageType): String {
    val processedMessage = getProcessedMessage(message)
    val index = processedMessage.indexOf("rs.")

    // If "rs." does not exist, return an empty string
    if (index == -1) {
        return ""
    }

    var money = processedMessage.getOrNull(index + 1) ?: ""

    money = money.replace(",", "")

    // If the data is a false positive
    // Look ahead one index and check for valid money
    return if (money.toDoubleOrNull() == null) {
        money = message.toString().getOrNull(index + 2)?.toString() ?: ""
        money = money.replace(",", "")

        // If this is also a false positive, return an empty string
        if (money.toDoubleOrNull() == null) {
            ""
        } else {
            padCurrencyValue(money)
        }
    } else {
        padCurrencyValue(money)
    }
}

fun getTransactionAmountForNonRs(message: MessageType): String {
    val processedMessage = getProcessedMessage(message)
    val index = processedMessage.indexOf("by")

    // If "rs." does not exist, return an empty string
    if (index == -1) {
        return ""
    }

    var money = processedMessage.getOrNull(index + 1) ?: ""

    money = money.replace(",", "")

    // If the data is a false positive
    // Look ahead one index and check for valid money
    return if (money.toDoubleOrNull() == null) {
        money = message.toString().getOrNull(index + 2)?.toString() ?: ""
        money = money.replace(",", "")

        // If this is also a false positive, return an empty string
        if (money.toDoubleOrNull() == null) {
            ""
        } else {
            padCurrencyValue(money)
        }
    } else {
        padCurrencyValue(money)
    }
}

fun getTransactionType(message: MessageType): TransactionType? {
    val creditPattern = Regex("(?:credited|credit|deposited|added|received|refund|repayment)", RegexOption.IGNORE_CASE)
    val debitPattern = Regex("(?:debited|debit|deducted)", RegexOption.IGNORE_CASE)
    val miscPattern = Regex(
        "(?:payment|spent|sent|paid|used|used\\s+at|charged|transaction\\son|transaction\\sfee|tran|booked|purchased|sent\\s+to|purchase\\s+of|spent\\s+on)",
        RegexOption.IGNORE_CASE
    )

    return when {
        debitPattern.containsMatchIn(message.toString()) -> "debit"
        miscPattern.containsMatchIn(message.toString()) -> "debit"
        creditPattern.containsMatchIn(message.toString()) -> "credit"
        else -> null
    }
}

fun isNonRsTypeSMSBody(transactionInfo: TransactionInfo): Boolean {
     return transactionInfo.account.type != null &&
            transactionInfo.account.number != null &&
            transactionInfo.transaction.type != null &&
            transactionInfo.transaction.amount == "";
}

fun getTransactionTypeForNonRs(messageType: MessageType, transactionInfo: TransactionInfo): TransactionInfo {
    val amount = getTransactionAmountForNonRs(messageType)

    return TransactionInfo(
        account = transactionInfo.account,
        balance = transactionInfo.balance,
        transaction = Transaction(
            type = transactionInfo.transaction.type,
            amount = amount,
            merchant = transactionInfo.transaction.merchant,
            referenceNo = transactionInfo.transaction.referenceNo
        )
    )
}

fun getTransactionInfo(message: String?): TransactionInfo {
    if (message.isNullOrEmpty()) {
        return TransactionInfo(
            account = AccountInfo(type = null, number = null, name = null),
            balance = null,
            transaction = Transaction(
                type = null,
                amount = null,
                merchant = null,
                referenceNo = null
            )
        )
    }

    val processedMessage = getProcessedMessage(message)

    val account = getAccount(processedMessage)
    val availableBalance = getBalance(processedMessage, BalanceKeyWordsType.AVAILABLE)
    val transactionAmount = getTransactionAmount(processedMessage)
    val isValid = listOf(availableBalance, transactionAmount, account?.number).count { !it.isNullOrEmpty() } >= 1
    val transactionType = if (isValid) getTransactionType(processedMessage) else null
    val balance = Balance(
        available = availableBalance,
        outstanding = if (account?.type == AccountType.CARD) {
            getBalance(processedMessage, BalanceKeyWordsType.OUTSTANDING)
        } else null
    )
    val merchantInfo = extractMerchantInfo(message)

    val transactionInfo = TransactionInfo(
        account = account,
        balance = balance,
        transaction = Transaction(
            type = transactionType,
            amount = transactionAmount,
            merchant = merchantInfo.merchant,
            referenceNo = merchantInfo.referenceNo
        )
    )

    // Fix for SBI sms where there is no RS or INR before the amount
    if (isNonRsTypeSMSBody(transactionInfo)) {
        return getTransactionTypeForNonRs(message, transactionInfo)
    }

    return transactionInfo
}

fun getCard(message: List<String>): AccountInfo {
    var combinedCardName = ""
    val cardIndex = message.indexOfFirst { word ->
        word == "card" || combinedWords // Any combined word of card type
            .filter { it.type == AccountType.CARD }
            .any { combinedWord ->
                if (combinedWord.word == word) {
                    combinedCardName = combinedWord.word
                    true
                } else {
                    false
                }
            }
    }

    val card = AccountInfo(type = null, name = null, number = null)

    // Search for "card" and if not found return empty object
    if (cardIndex != -1) {
        card.number = message.getOrNull(cardIndex + 1)
        card.type = AccountType.CARD

        // If the data is false positive, return empty object
        if (card.number?.toIntOrNull() == null) {
            return AccountInfo(
                type = if (combinedCardName.isNotEmpty()) card.type else null,
                name = combinedCardName,
                number = null
            )
        }
        return card
    }

    return AccountInfo(type = null, name = null, number = null)
}

fun getAccount(message: MessageType): AccountInfo {
    val processedMessage = getProcessedMessage(message)
    var accountIndex = -1
    var account = AccountInfo(type = null, name = null, number = null)

    for ((index, word) in processedMessage.withIndex()) {
        if (word == "ac") {
            if (index + 1 < processedMessage.size) {
                val accountNo = if (trimLeadingAndTrailingChars(processedMessage[index + 1]) != "") {
                    trimLeadingAndTrailingChars(processedMessage[index + 1])
                } else {
                    trimLeadingAndTrailingChars(processedMessage[index + 2])
                }

                if (accountNo.toIntOrNull() == null) {
                    // Continue searching for a valid account number
                    continue
                } else {
                    accountIndex = index
                    account = AccountInfo(type = AccountType.ACCOUNT, name = null, number = accountNo)
                    break
                }
            } else {
                // Continue searching for a valid account number
                continue
            }
        } else if (word.contains("ac")) {
            val extractedAccountNo = extractBondedAccountNo(word)

            if (extractedAccountNo.isEmpty()) {
                continue
            } else {
                accountIndex = index
                account = AccountInfo(type = AccountType.ACCOUNT, name = null, number = extractedAccountNo)
                break
            }
        }
    }

    // No occurrence of the word "ac". Check for "card"
    if (accountIndex == -1) {
        account = getCard(processedMessage)
    }

    // Check for wallets
    if (account.type == null) {
        val wallet = processedMessage.find { wallets.contains(it) }
        if (wallet != null) {
            account = AccountInfo(type = AccountType.WALLET, name = wallet, number = null)
        }
    }

    // Check for special accounts
    if (account.type == null) {
        val specialAccount = combinedWords
            .filter { it.type == AccountType.ACCOUNT }
            .find { processedMessage.contains(it.word) }

        account = AccountInfo(
            type = specialAccount?.type,
            name = specialAccount?.word,
            number = account.number
        )
    }

    // Extract last 4 digits of account number (e.g., 4334XXXXX4334)
    if (!account.number.isNullOrEmpty() && account.number!!.length > 4) {
        account.number = account.number!!.takeLast(4)
    }

    return account
}

fun extractBalance(index: Int, message: String, length: Int): String {
    var balance = ""
    var sawNumber = false
    var invalidCharCount = 0
    var char: Char
    var start = index

    while (start < length) {
        char = message[start]

        if (char in '0'..'9') {
            sawNumber = true
            balance += char
        } else if (sawNumber) {
            when {
                char == '.' -> {
                    if (invalidCharCount == 1) {
                        break
                    } else {
                        balance += char
                        invalidCharCount++
                    }
                }
                char != ',' -> {
                    break
                }
            }
        }

        start++
    }

    return balance
}

fun findNonStandardBalance(
    message: String,
    keyWordType: BalanceKeyWordsType = BalanceKeyWordsType.AVAILABLE
): String? {
    val balanceKeywords = if (keyWordType == BalanceKeyWordsType.AVAILABLE) {
        availableBalanceKeywords
    } else {
        outstandingBalanceKeywords
    }

    val balKeywordRegex = balanceKeywords.joinToString("|")
        .replace("/", "\\/")
    val amountRegex = """([\d]+\.[\d]+|[\d]+)"""

    // balance 100.00
    var regex = Regex("$balKeywordRegex\\s*$amountRegex", RegexOption.IGNORE_CASE)
    var matches = regex.find(message)
    if (matches != null) {
        val balance = matches.value.split(" ").lastOrNull()
        return if (balance?.toDoubleOrNull() == null) "" else balance
    }

    // 100.00 available
    regex = Regex("$amountRegex\\s*$balKeywordRegex", RegexOption.IGNORE_CASE)
    matches = regex.find(message)
    if (matches != null) {
        val balance = matches.value.split(" ").firstOrNull()
        return if (balance?.toDoubleOrNull() == null) "" else balance
    }

    return null
}

fun getBalance(
    message: MessageType,
    keyWordType: BalanceKeyWordsType = BalanceKeyWordsType.AVAILABLE
): String? {
    val processedMessage = getProcessedMessage(message)
    val messageString = processedMessage.joinToString(" ")
    var indexOfKeyword = -1
    var balance = ""

    val balanceKeywords = if (keyWordType == BalanceKeyWordsType.AVAILABLE) {
        availableBalanceKeywords
    } else {
        outstandingBalanceKeywords
    }

    // Find the index of the first occurrence of any balance keyword
    for (word in balanceKeywords) {
        indexOfKeyword = messageString.indexOf(word)
        if (indexOfKeyword != -1) {
            indexOfKeyword += word.length
            break
        }
    }

    // If a keyword was found, search for "rs." after the keyword
    var index = indexOfKeyword
    var indexOfRs = -1
    var nextThreeChars = if (index >= 0 && index + 3 <= messageString.length) {
        messageString.substring(index, index + 3)
    } else {
        ""
    }

    index += 3

    while (index < messageString.length) {
        nextThreeChars = nextThreeChars.drop(1) + messageString[index]
        if (nextThreeChars == "rs.") {
            indexOfRs = index + 2
            break
        }
        index++
    }

    // If "rs." was not found, check for non-standard balance
    if (indexOfRs == -1) {
        balance = findNonStandardBalance(messageString) ?: ""
        return if (balance.isNotEmpty()) padCurrencyValue(balance) else null
    }

    // Extract balance starting from the index after "rs."
    balance = extractBalance(indexOfRs, messageString, messageString.length)
    return if (balance.isNotEmpty()) padCurrencyValue(balance) else null
}
