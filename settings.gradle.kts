pluginManagement {
    repositories {
        google()
        jcenter()
        gradlePluginPortal()
        mavenCentral()
    }
    
}
rootProject.name = "RemoteCamera"

include(":libbambuser")
include(":libstreaming")
include(":androidApp")
include(":shared")