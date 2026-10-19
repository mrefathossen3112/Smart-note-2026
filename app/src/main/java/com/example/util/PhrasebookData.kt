package com.example.util

data class PhraseItem(
    val english: String,
    val translated: String,
    val pronunciation: String,
    val category: String
)

data class LanguagePhrasebook(
    val languageCode: String,
    val languageName: String,
    val flag: String,
    val phrases: List<PhraseItem>
)

object PhrasebookData {

    val languages: List<LanguagePhrasebook> = listOf(
        LanguagePhrasebook(
            languageCode = "ja",
            languageName = "Japanese",
            flag = "🇯🇵",
            phrases = listOf(
                PhraseItem("Hello", "こんにちは", "Konnichiwa", "Greetings"),
                PhraseItem("Thank you", "ありがとうございます", "Arigatou gozaimasu", "Greetings"),
                PhraseItem("Excuse me / Sorry", "すみません", "Sumimasen", "Greetings"),
                PhraseItem("Yes / No", "はい / いいえ", "Hai / Iie", "Greetings"),
                PhraseItem("Where is the station?", "駅はどこですか？", "Eki wa doko desu ka?", "Directions"),
                PhraseItem("Where is the toilet?", "トイレはどこですか？", "Toire wa doko desu ka?", "Directions"),
                PhraseItem("How much is this?", "これはいくらですか？", "Kore wa ikura desu ka?", "Shopping"),
                PhraseItem("Delicious!", "美味しいです！", "Oishii desu!", "Dining"),
                PhraseItem("Water please", "お水をください", "Omizu o kudasai", "Dining"),
                PhraseItem("The check / bill please", "お会計をお願いします", "Okaikei o onegaishimasu", "Dining"),
                PhraseItem("Please help me!", "助けてください！", "Tasukete kudasai!", "Emergency"),
                PhraseItem("Call a doctor", "医者を呼んでください", "Isha o yonde kudasai", "Emergency")
            )
        ),
        LanguagePhrasebook(
            languageCode = "es",
            languageName = "Spanish",
            flag = "🇪🇸",
            phrases = listOf(
                PhraseItem("Hello", "Hola", "OH-lah", "Greetings"),
                PhraseItem("Thank you very much", "Muchas gracias", "MOO-chas GRAH-syas", "Greetings"),
                PhraseItem("Excuse me / Pardon", "Disculpe / Por favor", "Dees-KOOL-peh", "Greetings"),
                PhraseItem("Where is the bathroom?", "¿Dónde está el baño?", "DON-deh eh-STAH el BAHN-yo?", "Directions"),
                PhraseItem("How much does it cost?", "¿Cuánto cuesta esto?", "KWAN-toh KWEH-stah EHS-toh?", "Shopping"),
                PhraseItem("The bill please", "La cuenta, por favor", "Lah KWEHN-tah poor fah-VOHR", "Dining"),
                PhraseItem("Do you have vegetarian food?", "¿Tiene comida vegetariana?", "TYEH-neh koh-MEE-dah", "Dining"),
                PhraseItem("I need help", "Necesito ayuda", "Neh-seh-SEE-toh ah-YOO-dah", "Emergency"),
                PhraseItem("Call the police", "Llame a la policía", "YAH-meh ah lah poh-lee-SEE-ah", "Emergency")
            )
        ),
        LanguagePhrasebook(
            languageCode = "fr",
            languageName = "French",
            flag = "🇫🇷",
            phrases = listOf(
                PhraseItem("Hello / Good morning", "Bonjour", "Bohn-ZHOOR", "Greetings"),
                PhraseItem("Thank you very much", "Merci beaucoup", "Mehr-SEE boh-KOO", "Greetings"),
                PhraseItem("Please", "S'il vous plaît", "Seel voo pleh", "Greetings"),
                PhraseItem("Where is the train station?", "Où est la gare?", "Oo eh lah gahr?", "Directions"),
                PhraseItem("How much is it?", "Combien ça coûte?", "Kohm-byan sah koot?", "Shopping"),
                PhraseItem("The check please", "L'addition, s'il vous plaît", "Lah-dee-syohn seel voo pleh", "Dining"),
                PhraseItem("Help me please!", "Aidez-moi s'il vous plaît!", "Eh-day mwah seel voo pleh!", "Emergency"),
                PhraseItem("I am lost", "Je suis perdu(e)", "Zhuh swee pehr-dyoo", "Emergency")
            )
        ),
        LanguagePhrasebook(
            languageCode = "it",
            languageName = "Italian",
            flag = "🇮🇹",
            phrases = listOf(
                PhraseItem("Hello / Bye", "Ciao / Buongiorno", "CHOW / bwon-JOHR-noh", "Greetings"),
                PhraseItem("Thank you", "Grazie mille", "GRAHT-tsyeh MEEL-leh", "Greetings"),
                PhraseItem("Where is the restaurant?", "Dov'è il ristorante?", "Doh-VEH eel ree-stoh-RAHN-teh?", "Directions"),
                PhraseItem("How much is this?", "Quanto costa questo?", "KWAHN-toh KOH-stah KWEH-stoh?", "Shopping"),
                PhraseItem("Delicious!", "Delizioso!", "Deh-lee-TSYOH-zoh!", "Dining"),
                PhraseItem("I need a doctor", "Ho bisogno di un medico", "Oh bee-ZOHN-yoh dee oon MEH-dee-koh", "Emergency")
            )
        ),
        LanguagePhrasebook(
            languageCode = "ar",
            languageName = "Arabic",
            flag = "🇦🇪",
            phrases = listOf(
                PhraseItem("Peace be upon you (Hello)", "السلام عليكم", "As-salamu alaykum", "Greetings"),
                PhraseItem("Thank you", "شكراً جزيلاً", "Shukran jazeelan", "Greetings"),
                PhraseItem("Where is the hotel?", "أين الفندق؟", "Ayna al-funduq?", "Directions"),
                PhraseItem("How much is this?", "بكم هذا؟", "Bikam hadha?", "Shopping"),
                PhraseItem("Water please", "ماء من فضلك", "Maa' min fadlik", "Dining"),
                PhraseItem("Help me please", "ساعدني من فضلك", "Saa'idnee min fadlik", "Emergency")
            )
        ),
        LanguagePhrasebook(
            languageCode = "th",
            languageName = "Thai",
            flag = "🇹🇭",
            phrases = listOf(
                PhraseItem("Hello", "สวัสดี (Sawadee khrup/ka)", "Sah-wah-dee", "Greetings"),
                PhraseItem("Thank you", "ขอบคุณ (Khob khun khrup/ka)", "Kob-koon", "Greetings"),
                PhraseItem("How much is this?", "อันนี้เท่าไหร่ (An nee tao rai?)", "Ahn nee tow-rye?", "Shopping"),
                PhraseItem("Not spicy please", "ไม่เผ็ดครับ/ค่ะ (Mai phet)", "My pet", "Dining"),
                PhraseItem("Where is the airport?", "สนามบินอยู่ที่ไหน (Sanam bin yoo tee nai?)", "Sah-nahm-been yoo tee-nye?", "Directions"),
                PhraseItem("Help!", "ช่วยด้วย (Chuay duay!)", "Choo-ay doo-ay!", "Emergency")
            )
        ),
        LanguagePhrasebook(
            languageCode = "bn",
            languageName = "Bengali",
            flag = "🇧🇩",
            phrases = listOf(
                PhraseItem("Hello / Greetings", "নমস্কার / আসসালামু আলাইকুম", "Nomoshkar / Assalamu Alaikum", "Greetings"),
                PhraseItem("Thank you very much", "অনেক ধন্যবাদ", "Onek Dhonnobad", "Greetings"),
                PhraseItem("Where is this place?", "এই জায়গাটি কোথায়?", "Ei jaigati kothay?", "Directions"),
                PhraseItem("How much is this?", "এটার দাম কত?", "Etar daam koto?", "Shopping"),
                PhraseItem("The food is very good", "খাবারটি খুব চমৎকার", "Khabarti khub chomokkar", "Dining"),
                PhraseItem("Please help me", "আমাকে একটু সাহায্য করুন", "Amake ektu sahajjo korun", "Emergency")
            )
        )
    )
}
