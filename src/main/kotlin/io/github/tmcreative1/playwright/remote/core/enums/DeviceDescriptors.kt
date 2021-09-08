package com.playwright.remote.core.enums

import com.playwright.remote.engine.options.ScreenSize
import com.playwright.remote.engine.options.ViewportSize

enum class DeviceDescriptors(
    val userAgent: String,
    val viewport: ViewportSize,
    val deviceScaleFactor: Double,
    val screen: ScreenSize? = null,
    val isMobile: Boolean = true,
    val hasTouch: Boolean = true
) {

    BLACKBERRY_PLAYBOOK(
        "Mozilla/5.0 (PlayBook; U; RIM Tablet OS 2.1.0; en-US) AppleWebKit/536.2+ (KHTML like Gecko) Version/7.2.1.0 Safari/536.2+",
        ViewportSize {
            it.width = 600
            it.height = 1024
        },
        1.0,
    ),
    BLACKBERRY_PLAYBOOK_LANDSCAPE(
        "Mozilla/5.0 (PlayBook; U; RIM Tablet OS 2.1.0; en-US) AppleWebKit/536.2+ (KHTML like Gecko) Version/7.2.1.0 Safari/536.2+",
        ViewportSize {
            it.width = 1024
            it.height = 600
        },
        1.0,
    ),
    BLACKBERRY_Z30(
        "Mozilla/5.0 (BB10; Touch) AppleWebKit/537.10+ (KHTML, like Gecko) Version/10.0.9.2372 Mobile Safari/537.10+",
        ViewportSize {
            it.width = 360
            it.height = 640
        },
        2.0,
    ),
    BLACKBERRY_Z30_LANDSCAPE(
        "Mozilla/5.0 (BB10; Touch) AppleWebKit/537.10+ (KHTML, like Gecko) Version/10.0.9.2372 Mobile Safari/537.10+",
        ViewportSize {
            it.width = 640
            it.height = 360
        },
        2.0,
    ),
    GALAXY_NOTE_3(
        "userAgent': 'Mozilla/5.0 (Linux; U; Android 4.3; en-us; SM-N900T Build/JSS15J) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
        ViewportSize {
            it.width = 360
            it.height = 640
        },
        3.0,
    ),
    GALAXY_NOTE_3_LANDSCAPE(
        "userAgent': 'Mozilla/5.0 (Linux; U; Android 4.3; en-us; SM-N900T Build/JSS15J) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
        ViewportSize {
            it.width = 640
            it.height = 360
        },
        3.0,
    ),
    GALAXY_NOTE_II(
        "userAgent': 'Mozilla/5.0 (Linux; U; Android 4.1; en-us; GT-N7100 Build/JRO03C) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
        ViewportSize {
            it.width = 360
            it.height = 640
        },
        2.0,
    ),
    GALAXY_NOTE_II_LANDSCAPE(
        "userAgent': 'Mozilla/5.0 (Linux; U; Android 4.1; en-us; GT-N7100 Build/JRO03C) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
        ViewportSize {
            it.width = 640
            it.height = 360
        },
        2.0,
    ),
    GALAXY_S_III(
        "Mozilla/5.0 (Linux; U; Android 4.0; en-us; GT-I9300 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
        ViewportSize {
            it.width = 360
            it.height = 640
        },
        2.0,
    ),
    GALAXY_S_III_LANDSCAPE(
        "Mozilla/5.0 (Linux; U; Android 4.0; en-us; GT-I9300 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
        ViewportSize {
            it.width = 640
            it.height = 360
        },
        2.0,
    ),
    GALAXY_S5(
        "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 360
            it.height = 640
        },
        3.0,
    ),
    GALAXY_S5_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 640
            it.height = 360
        },
        3.0,
    ),
    GALAXY_S8(
        "Mozilla/5.0 (Linux; Android 7.0; SM-G950U Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36",
        ViewportSize {
            it.width = 360
            it.height = 740
        },
        3.0,
    ),
    GALAXY_S8_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 7.0; SM-G950U Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36",
        ViewportSize {
            it.width = 740
            it.height = 360
        },
        3.0,
    ),
    GALAXY_S9_PLUS(
        "Mozilla/5.0 (Linux; Android 8.0.0; SM-G965U Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.111 Mobile Safari/537.36",
        ViewportSize {
            it.width = 320
            it.height = 658
        },
        4.5,
    ),
    GALAXY_S9_PLUS_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 8.0.0; SM-G965U Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.111 Mobile Safari/537.36",
        ViewportSize {
            it.width = 658
            it.height = 320
        },
        4.5,
    ),
    GALAXY_TAB_S4(
        "Mozilla/5.0 (Linux; Android 8.1.0; SM-T837A) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.80 Safari/537.36",
        ViewportSize {
            it.width = 712
            it.height = 1138
        },
        2.25,
    ),
    GALAXY_TAB_S4_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 8.1.0; SM-T837A) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.80 Safari/537.36",
        ViewportSize {
            it.width = 1138
            it.height = 712
        },
        2.25,
    ),
    IPAD_GEN_6(
        "Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 758
            it.height = 1024
        },
        2.0,
    ),
    IPAD_GEN_6_LANDSCAPE(
        "Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 1024
            it.height = 758
        },
        2.0,
    ),
    IPAD_GEN_7(
        "Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 810
            it.height = 1080
        },
        2.0,
    ),
    IPAD_GEN_7_LANDSCAPE(
        "Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 1080
            it.height = 810
        },
        2.0,
    ),
    IPAD_MINI(
        "Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 768
            it.height = 1024
        },
        2.0,
    ),
    IPAD_MINI_LANDSCAPE(
        "Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 1024
            it.height = 768
        },
        2.0,
    ),
    IPAD_PRO_11(
        "Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 834
            it.height = 1194
        },
        2.0
    ),
    IPAD_PRO_11_LANDSCAPE(
        "Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 1194
            it.height = 834
        },
        2.0
    ),
    IPHONE_6(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
        ViewportSize {
            it.width = 375
            it.height = 667
        },
        2.0
    ),
    IPHONE_6_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
        ViewportSize {
            it.width = 667
            it.height = 375
        },
        2.0
    ),
    IPHONE_6_PLUS(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
        ViewportSize {
            it.width = 414
            it.height = 736
        },
        3.0
    ),
    IPHONE_6_PLUS_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
        ViewportSize {
            it.width = 736
            it.height = 414
        },
        3.0
    ),
    IPHONE_7(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
        ViewportSize {
            it.width = 375
            it.height = 667
        },
        2.0
    ),
    IPHONE_7_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
        ViewportSize {
            it.width = 667
            it.height = 375
        },
        2.0
    ),
    IPHONE_7_PLUS(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
        ViewportSize {
            it.width = 414
            it.height = 736
        },
        3.0
    ),
    IPHONE_7_PLUS_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
        ViewportSize {
            it.width = 736
            it.height = 414
        },
        3.0
    ),
    IPHONE_8(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
        ViewportSize {
            it.width = 375
            it.height = 667
        },
        2.0
    ),
    IPHONE_8_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
        ViewportSize {
            it.width = 375
            it.height = 667
        },
        2.0
    ),
    IPHONE_8_PLUS(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
        ViewportSize {
            it.width = 414
            it.height = 736
        },
        3.0
    ),
    IPHONE_8_PLUS_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
        ViewportSize {
            it.width = 736
            it.height = 414
        },
        3.0
    ),
    IPHONE_SE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1",
        ViewportSize {
            it.width = 320
            it.height = 568
        },
        2.0
    ),
    IPHONE_SE_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1",
        ViewportSize {
            it.width = 568
            it.height = 320
        },
        2.0
    ),
    IPHONE_X(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
        ViewportSize {
            it.width = 375
            it.height = 812
        },
        3.0
    ),
    IPHONE_X_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
        ViewportSize {
            it.width = 812
            it.height = 375
        },
        3.0
    ),
    IPHONE_XR(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 414
            it.height = 896
        },
        3.0
    ),
    IPHONE_XR_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 896
            it.height = 414
        },
        3.0
    ),
    IPHONE_11(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 414
            it.height = 715
        },
        2.0,
        ScreenSize {
            it.width = 414
            it.height = 896
        }
    ),
    IPHONE_11_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 800
            it.height = 364
        },
        2.0,
        ScreenSize {
            it.width = 414
            it.height = 896
        }
    ),
    IPHONE_11_PRO(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 375
            it.height = 635
        },
        3.0,
        ScreenSize {
            it.width = 375
            it.height = 812
        }
    ),
    IPHONE_11_PRO_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 724
            it.height = 325
        },
        3.0,
        ScreenSize {
            it.width = 375
            it.height = 812
        }
    ),
    IPHONE_11_PRO_MAX(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 414
            it.height = 715
        },
        3.0,
        ScreenSize {
            it.width = 414
            it.height = 896
        }
    ),
    IPHONE_11_PRO_MAX_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 808
            it.height = 364
        },
        3.0,
        ScreenSize {
            it.width = 414
            it.height = 896
        }
    ),
    IPHONE_12(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 390
            it.height = 664
        },
        3.0,
        ScreenSize {
            it.width = 390
            it.height = 844
        }
    ),
    IPHONE_12_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 750
            it.height = 340
        },
        3.0,
        ScreenSize {
            it.width = 390
            it.height = 844
        }
    ),
    IPHONE_12_PRO(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 390
            it.height = 664
        },
        3.0,
        ScreenSize {
            it.width = 390
            it.height = 844
        }
    ),
    IPHONE_12_PRO_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 750
            it.height = 340
        },
        3.0,
        ScreenSize {
            it.width = 390
            it.height = 844
        }
    ),
    IPHONE_12_PRO_MAX(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 428
            it.height = 746
        },
        3.0,
        ScreenSize {
            it.width = 428
            it.height = 926
        }
    ),
    IPHONE_12_PRO_MAX_LANDSCAPE(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Mobile/15E148 Safari/604.1",
        ViewportSize {
            it.width = 832
            it.height = 378
        },
        3.0,
        ScreenSize {
            it.width = 428
            it.height = 926
        }
    ),
    JIO_PHONE_2(
        "Mozilla/5.0 (Mobile; LYF/F300B/LYF-F300B-001-01-15-130718-i;Android; rv:48.0) Gecko/48.0 Firefox/48.0 KAIOS/2.5",
        ViewportSize {
            it.width = 240
            it.height = 320
        },
        1.0
    ),
    JIO_PHONE_2_LANDSCAPE(
        "Mozilla/5.0 (Mobile; LYF/F300B/LYF-F300B-001-01-15-130718-i;Android; rv:48.0) Gecko/48.0 Firefox/48.0 KAIOS/2.5",
        ViewportSize {
            it.width = 320
            it.height = 240
        },
        1.0
    ),
    KINDLE_FIRE_HDX(
        "Mozilla/5.0 (Linux; U; en-us; KFAPWI Build/JDQ39) AppleWebKit/535.19 (KHTML, like Gecko) Silk/3.13 Safari/535.19 Silk-Accelerated=true",
        ViewportSize {
            it.width = 800
            it.height = 1280
        },
        2.0
    ),
    KINDLE_FIRE_HDX_LANDSCAPE(
        "Mozilla/5.0 (Linux; U; en-us; KFAPWI Build/JDQ39) AppleWebKit/535.19 (KHTML, like Gecko) Silk/3.13 Safari/535.19 Silk-Accelerated=true",
        ViewportSize {
            it.width = 1280
            it.height = 800
        },
        2.0
    ),
    LG_OPTIMUS_L70(
        "Mozilla/5.0 (Linux; U; Android 4.4.2; en-us; LGMS323 Build/KOT49I.MS32310c) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/75.0.3765.0 Mobile Safari/537.3",
        ViewportSize {
            it.width = 384
            it.height = 640
        },
        1.25
    ),
    LG_OPTIMUS_L70_LANDSCAPE(
        "Mozilla/5.0 (Linux; U; Android 4.4.2; en-us; LGMS323 Build/KOT49I.MS32310c) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/75.0.3765.0 Mobile Safari/537.3",
        ViewportSize {
            it.width = 640
            it.height = 384
        },
        1.25
    ),
    MICROSOFT_LUMIA_550(
        "Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Microsoft; Lumia 550) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/14.14263",
        ViewportSize {
            it.width = 640
            it.height = 360
        },
        2.0
    ),
    MICROSOFT_LUMIA_550_LANDSCAPE(
        "Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Microsoft; Lumia 550) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/14.14263",
        ViewportSize {
            it.width = 360
            it.height = 640
        },
        2.0
    ),
    MICROSOFT_LUMIA_950(
        "Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Microsoft; Lumia 950) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/14.14263",
        ViewportSize {
            it.width = 360
            it.height = 640
        },
        4.0
    ),
    MICROSOFT_LUMIA_950_LANDSCAPE(
        "Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Microsoft; Lumia 950) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/14.14263",
        ViewportSize {
            it.width = 640
            it.height = 360
        },
        4.0
    ),
    NEXUS_10(
        "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 10 Build/MOB31T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Safari/537.36",
        ViewportSize {
            it.width = 800
            it.height = 1280
        },
        2.0
    ),
    NEXUS_10_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 10 Build/MOB31T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Safari/537.36",
        ViewportSize {
            it.width = 1280
            it.height = 800
        },
        2.0
    ),
    NEXUS_4(
        "Mozilla/5.0 (Linux; Android 4.4.2; Nexus 4 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 384
            it.height = 640
        },
        2.0
    ),
    NEXUS_4_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 4.4.2; Nexus 4 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 640
            it.height = 384
        },
        2.0
    ),
    NEXUS_5(
        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 360
            it.height = 640
        },
        3.0
    ),
    NEXUS_5_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 640
            it.height = 360
        },
        3.0
    ),
    NEXUS_5X(
        "Mozilla/5.0 (Linux; Android 8.0.0; Nexus 5X Build/OPR4.170623.006) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 412
            it.height = 732
        },
        2.625
    ),
    NEXUS_5X_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 8.0.0; Nexus 5X Build/OPR4.170623.006) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 732
            it.height = 412
        },
        2.625
    ),
    NEXUS_6(
        "Mozilla/5.0 (Linux; Android 7.1.1; Nexus 6 Build/N6F26U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 412
            it.height = 732
        },
        3.5
    ),
    NEXUS_6_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 7.1.1; Nexus 6 Build/N6F26U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 732
            it.height = 412
        },
        3.5
    ),
    NEXUS_6P(
        "Mozilla/5.0 (Linux; Android 8.0.0; Nexus 6P Build/OPP3.170518.006) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 412
            it.height = 732
        },
        3.5
    ),
    NEXUS_6P_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 8.0.0; Nexus 6P Build/OPP3.170518.006) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 732
            it.height = 412
        },
        3.5
    ),
    NEXUS_7(
        "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 7 Build/MOB30X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Safari/537.36",
        ViewportSize {
            it.width = 600
            it.height = 960
        },
        2.0
    ),
    NEXUS_7_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 7 Build/MOB30X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Safari/537.36",
        ViewportSize {
            it.width = 960
            it.height = 600
        },
        2.0
    ),
    NOKIA_LUMIA_520(
        "Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; NOKIA; Lumia 520)",
        ViewportSize {
            it.width = 320
            it.height = 533
        },
        1.5
    ),
    NOKIA_LUMIA_520_LANDSCAPE(
        "Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; NOKIA; Lumia 520)",
        ViewportSize {
            it.width = 533
            it.height = 320
        },
        1.5
    ),
    NOKIA_N9(
        "Mozilla/5.0 (MeeGo; NokiaN9) AppleWebKit/534.13 (KHTML, like Gecko) NokiaBrowser/8.5.0 Mobile Safari/534.13",
        ViewportSize {
            it.width = 480
            it.height = 854
        },
        1.0
    ),
    NOKIA_N9_LANDSCAPE(
        "Mozilla/5.0 (MeeGo; NokiaN9) AppleWebKit/534.13 (KHTML, like Gecko) NokiaBrowser/8.5.0 Mobile Safari/534.13",
        ViewportSize {
            it.width = 854
            it.height = 480
        },
        1.0
    ),
    PIXEL_2(
        "Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 411
            it.height = 731
        },
        2.625
    ),
    PIXEL_2_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 731
            it.height = 411
        },
        2.625
    ),
    PIXEL_2_XL(
        "Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 411
            it.height = 823
        },
        3.5
    ),
    PIXEL_2_XL_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",
        ViewportSize {
            it.width = 823
            it.height = 411
        },
        3.5
    ),
    PIXEL_3(
        "Mozilla/5.0 (Linux; Android 9; Pixel 3 Build/PQ1A.181105.017.A1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.158 Mobile Safari/537.36",
        ViewportSize {
            it.width = 393
            it.height = 786
        },
        2.75
    ),
    PIXEL_3_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 9; Pixel 3 Build/PQ1A.181105.017.A1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.158 Mobile Safari/537.36",
        ViewportSize {
            it.width = 786
            it.height = 393
        },
        2.75
    ),
    PIXEL_4(
        "Mozilla/5.0 (Linux; Android 10; Pixel 4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Mobile Safari/537.36",
        ViewportSize {
            it.width = 353
            it.height = 745
        },
        3.0
    ),
    PIXEL_4_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 10; Pixel 4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Mobile Safari/537.36",
        ViewportSize {
            it.width = 745
            it.height = 353
        },
        3.0
    ),
    PIXEL_4A_5G(
        "Mozilla/5.0 (Linux; Android 11; Pixel 4a (5G)) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.105 Mobile Safari/537.36",
        ViewportSize {
            it.width = 412
            it.height = 765
        },
        2.63,
        ScreenSize {
            it.width = 412
            it.height = 892
        }
    ),
    PIXEL_4A_5G_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 11; Pixel 4a (5G)) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.105 Mobile Safari/537.36",
        ViewportSize {
            it.width = 840
            it.height = 312
        },
        2.63,
        ScreenSize {
            it.width = 412
            it.height = 892
        }
    ),
    PIXEL_5(
        "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.105 Mobile Safari/537.36",
        ViewportSize {
            it.width = 393
            it.height = 727
        },
        2.75,
        ScreenSize {
            it.width = 393
            it.height = 851
        }
    ),
    PIXEL_5_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.105 Mobile Safari/537.36",
        ViewportSize {
            it.width = 802
            it.height = 293
        },
        2.75,
        ScreenSize {
            it.width = 851
            it.height = 393
        }
    ),
    MOTO_G64(
        "Mozilla/5.0 (Linux; Android 7.0; Moto G (4)) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4143.7 Mobile Safari/537.36",
        ViewportSize {
            it.width = 360
            it.height = 640
        },
        3.0
    ),
    MOTO_G64_LANDSCAPE(
        "Mozilla/5.0 (Linux; Android 7.0; Moto G (4)) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4143.7 Mobile Safari/537.36",
        ViewportSize {
            it.width = 640
            it.height = 360
        },
        3.0
    )
    ;
}