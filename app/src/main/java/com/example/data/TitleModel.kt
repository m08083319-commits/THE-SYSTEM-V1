package com.example.data

data class PlayerTitle(
    val id: String,
    val name: String,
    val englishName: String,
    val description: String,
    val requirement: String,
    val rarity: String,
    val rarityColor: Long,
    val perkType: String,
    val perkValue: Double
) {
    companion object {
        const val RARITY_COMMON = "COMMON"
        const val RARITY_UNCOMMON = "UNCOMMON"
        const val RARITY_RARE = "RARE"
        const val RARITY_LEGENDARY = "LEGENDARY"

        val ALL_TITLES = listOf(
            PlayerTitle(
                id = "title_dawn_son",
                name = "ابن الفجر",
                englishName = "Son of the Dawn",
                description = "مكافآت المهام الصباحية تزداد بنسبة 10%.",
                requirement = "أكمل مهمة فجر واحدة.",
                rarity = RARITY_RARE,
                rarityColor = 0xFF4DA6FF,
                perkType = "PERK_MORNING_XP",
                perkValue = 1.10
            ),
            PlayerTitle(
                id = "title_dawn_guardian",
                name = "حارس الفجر",
                englishName = "Guardian of the Dawn",
                description = "مكافآت المهام الصباحية +25% ومهمة الفجر تمتد 30 دقيقة (حتى 6:30 ص).",
                requirement = "أكمل 10 مهام فجر.",
                rarity = RARITY_LEGENDARY,
                rarityColor = 0xFFFFD700,
                perkType = "PERK_MORNING_EXPAND",
                perkValue = 1.25
            ),
            PlayerTitle(
                id = "title_streak_week",
                name = "أسبوع الصمود",
                englishName = "Week of Endurance",
                description = "٧ أيام متتالية بلا عقوبة. AP +5%.",
                requirement = "أكمل سلسلة من 7 أيام.",
                rarity = RARITY_UNCOMMON,
                rarityColor = 0xFF10B981,
                perkType = "PERK_AP_BONUS",
                perkValue = 1.05
            ),
            PlayerTitle(
                id = "title_streak_month",
                name = "الشهر الكامل",
                englishName = "Full Month",
                description = "٣٠ يوم متتالية. AP +10%.",
                requirement = "أكمل سلسلة من 30 يوم.",
                rarity = RARITY_RARE,
                rarityColor = 0xFF4DA6FF,
                perkType = "PERK_AP_BONUS",
                perkValue = 1.10
            ),
            PlayerTitle(
                id = "title_streak_hundred",
                name = "مئة يوم من النار",
                englishName = "Hundred Days of Fire",
                description = "١٠٠ يوم متتالية. AP +15%.",
                requirement = "أكمل سلسلة من 100 يوم.",
                rarity = RARITY_RARE,
                rarityColor = 0xFF8B5CF6,
                perkType = "PERK_AP_BONUS",
                perkValue = 1.15
            ),
            PlayerTitle(
                id = "title_streak_year",
                name = "لا شيء يوقفه",
                englishName = "Unstoppable",
                description = "٣٦٥ يوم متتالية. AP +25% ومقاومة عقوبات 50%.",
                requirement = "أكمل سلسلة من 365 يوم.",
                rarity = RARITY_LEGENDARY,
                rarityColor = 0xFFFFD700,
                perkType = "PERK_AP_BONUS",
                perkValue = 1.25
            ),
            PlayerTitle(
                id = "title_fragile",
                name = "وعاء بشري هش",
                englishName = "Fragile Vessel",
                description = "اللقب الاستهلالي. لا يمنح أي خصائص إضافية ملموسة، لكنه يمثل نقطة انطلاق الوعاء للصحوة.",
                requirement = "ممنوح تلقائياً عند الدخول الأول للنظام.",
                rarity = RARITY_COMMON,
                rarityColor = 0xFF3399FF, // Bright Blue
                perkType = "NONE",
                perkValue = 0.0
            ),
            PlayerTitle(
                id = "title_sweeper",
                name = "كاسح المغارات",
                englishName = "Dungeon Sweeper",
                description = "زيادة دائمة بمقدار +15% للذهب المكتسب من تصفية مهام بروتوكول الفجر بنجاح.",
                requirement = "الوصول لسلسلة إكمال مهام متتالية (Streak) تبلغ 5 أيام فأكثر.",
                rarity = RARITY_UNCOMMON,
                rarityColor = 0xFF33CC33, // Uncommon Green
                perkType = "GOLD_BOOST",
                perkValue = 0.15
            ),
            PlayerTitle(
                id = "title_shadow_monarch",
                name = "ملك الظلال",
                englishName = "Shadow Monarch",
                description = "زيادة سحرية تفاعلية للتحكم بالظلال؛ تخفيض تكلفة استخلاص جنود الظلال بنسبة 20%- وتسهيل إطاعة الوعاء.",
                requirement = "الوصول إلى مستوى الوعاء 10 مع ترقية كافة الخصائص الأساسية المتزامة.",
                rarity = RARITY_LEGENDARY,
                rarityColor = 0xFFFFD700, // Legendary Gold
                perkType = "MANA_COST_REDUNDANCY",
                perkValue = 0.20
            ),
            PlayerTitle(
                id = "title_sovereign_light",
                name = "سيد الضوء",
                englishName = "Sovereign of Light",
                description = "زيادة دائمة لجميع مكافآت نقاط الخبرة (XP) بنسبة +15% لتسهيل زيادة تماسك واستقرار الوعاء.",
                requirement = "الوصول إلى 5,000 قطعة ذهبية في رصيد الخزينة، أو تصفية 15 تمرين.",
                rarity = RARITY_LEGENDARY,
                rarityColor = 0xFFFFD700, // Legendary Gold
                perkType = "XP_BOOST",
                perkValue = 0.15
            ),
            PlayerTitle(
                id = "title_gate_tamer",
                name = "مروض البوابات",
                englishName = "Gate Tamer",
                description = "زيادة دائمة بمعدل +10% للحد الأقصى أو كسب طاقة الوعاء السحرية (Mana Stored) لتسهيل اقتحام المغارات.",
                requirement = "الوصول لمستوى الوعاء 15 أو تسجيل 5 إكمالات كاملة لبوابات المغامرة.",
                rarity = RARITY_RARE,
                rarityColor = 0xFF8A2BE2, // Rare Purple
                perkType = "MP_BOOST",
                perkValue = 0.10
            ),
            PlayerTitle(
                id = "title_abyss_devourer",
                name = "مبتلع الهاوية",
                englishName = "Abyss Devourer",
                description = "فرصة 10% لمضاعفة شظايا الهاوية (Abyss Shards) المكتسبة عند تصفية أي بوابة.",
                requirement = "امتلاك أو جمع ما يزيد عن 1000 شظية هاوية في رصيد الصياد العتيد.",
                rarity = RARITY_RARE,
                rarityColor = 0xFF8A2BE2, // Rare Purple
                perkType = "SHARD_CHANCE",
                perkValue = 0.10
            ),
            PlayerTitle(
                id = "title_steel_will",
                name = "العزيمة الفولاذية",
                englishName = "Steel Will",
                description = "زيادة فرص بقاء الوعاء؛ تخفيض الضرر البدني الناشئ من العقاب وارتفاع صلابة تماسك الوعاء بنسبة 10%+.",
                requirement = "تحقيق سلسلة متتالية (Streak) من الطمأنينة تصل لـ 10 أيام دون تلقي أي عقوبة.",
                rarity = RARITY_RARE,
                rarityColor = 0xFF8A2BE2, // Rare Purple
                perkType = "WILL_PROTECT",
                perkValue = 0.10
            ),
            PlayerTitle(
                id = "title_lord_wisdom",
                name = "سيد الحكمة والذكاء",
                englishName = "Lord of Wisdom",
                description = "طاقة فكرية مركزة؛ زيادة دائمة بنسبة +10% لكسب نقاط الـ AP (نقاط القدرة) من مختلف النشاطات اليومية.",
                requirement = "تطوير النبوغ العصبي للوعاء (INT/Intellect) لأكثر من 15 نقطة بنجاح.",
                rarity = RARITY_UNCOMMON,
                rarityColor = 0xFF33CC33, // Uncommon Green
                perkType = "AP_BOOST",
                perkValue = 0.10
            ),
            PlayerTitle(
                id = "title_impostor",
                name = "المخادع",
                englishName = "The Impostor",
                description = "لعنة مستديمة ناتجة عن التلاعب بأجهزة النظام للغش في التمارين. كافة المكافآت تنخفض بنسبة -20%.",
                requirement = "يُطبَّق إجبارياً عند رصد 4 أو أكثر من محاولات الغش غير المشروعة.",
                rarity = "CURSE",
                rarityColor = 0xFFFF0044, // Vibrant Red
                perkType = "CURSE_ALL_REWARDS",
                perkValue = -0.20
            )
        )
    }
}
