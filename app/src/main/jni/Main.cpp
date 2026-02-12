#include <list>
#include <vector>
#include <cstring>
#include <pthread.h>
#include <thread>
#include <cstring>
#include <string>
#include <jni.h>
#include <unistd.h>
#include <fstream>
#include <iostream>
#include <dlfcn.h>
#include "Includes/Logger.h"
#include "Includes/obfuscate.h"
#include "Includes/Utils.hpp"
#include "Menu/Menu.hpp"
#include "Menu/Jni.hpp"
#include "Includes/Macros.h"

#include "offsets.h"
#include "Dobby/dobby.h"

struct MemPatches {
    MemoryPatch noDeath;
    // etc...
} gPatches;




//Target lib here
#define targetLibName OBFUSCATE("libil2cpp.so")
ElfScanner g_il2cppELF;


int PlayerSpeedValue = 1, KillsValue = 1, deadsValue = 1;
bool FastRespawn;

void *PlayerInstance;
void *GadgetInstance;
void (*SetHP)(void *instance, int num);



void (*org_PlayerUpdate)(void *instance);
void Hook_PlayerUpdate(void *instance) {
    if (instance != NULL) {
        if (instance != PlayerInstance) {
            PlayerInstance = instance;
        }
        if (PlayerSpeedValue > 1) *(float *)((uintptr_t)instance + Fields64::playerSpeed) = PlayerSpeedValue;
        
        if (deadsValue > 1) *(int *)((uintptr_t)instance + Fields64::deathCount) = deadsValue;
        
        if (KillsValue > 1) *(int *)((uintptr_t)instance + Fields64::killCount) = KillsValue;
        
        if (FastRespawn) *(float *)((uintptr_t)instance + Fields64::respawnTimer) = 0; 
    }
    return org_PlayerUpdate(instance);
}


void (*org_WeaponShoot)(void *instance);
void Hook_WeaponShoot(void *instance) {
    if (instance != NULL) { 
        if (instance != GadgetInstance) GadgetInstance = instance;
    }
    return org_WeaponShoot(instance);
}

void InstallHooks() {
    DobbyHook((void *)getAbsoluteAddress(targetLibName, Offsets64::PlayerFixedUpdate), (void *)Hook_PlayerUpdate, (void **)&org_PlayerUpdate);
    DobbyHook((void *)getAbsoluteAddress(targetLibName, Offsets64::WeaponShoot), (void *)Hook_WeaponShoot, (void **)&org_WeaponShoot);
    
    
    SetHP = (void (*)(void *, int)) getAbsoluteAddress(targetLibName, Offsets64::set_Health);
}





jobjectArray GetFeatureList(JNIEnv *env, jobject context) {
    jobjectArray ret;

    const char *features[] = {
            OBFUSCATE("Tab_Combat Mods"),
            OBFUSCATE("1_TabAdd_Toggle_Unlimited Ammo"),
            OBFUSCATE("2_TabAdd_Toggle_One Hit Damage"),
            OBFUSCATE("3_TabAdd_Toggle_Fire rate"),
            OBFUSCATE("4_TabAdd_Toggle_Fire Speed"),
            OBFUSCATE("5_TabAdd_Toggle_built Speed"),
            OBFUSCATE("6_TabAdd_Toggle_No Over Heated"),
            OBFUSCATE("7_TabAdd_Toggle_Infinity Grenade"),
            OBFUSCATE("8_TabAdd_Toggle_No Reload"),
            OBFUSCATE("9_TabAdd_SeekBar_built Range_1_100"),
            OBFUSCATE("10_TabAdd_Toggle_Aimbot"),
            OBFUSCATE("Tab_Player Mods"),
            OBFUSCATE("11_TabAdd_Toggle_Unlimted HP"),
            OBFUSCATE("12_TabAdd_Toggle_Fast Regen"),
            OBFUSCATE("13_TabAdd_Toggle_Fast Respawn"),
            OBFUSCATE("14_TabAdd_InputValue_Set Kills_100"),
            OBFUSCATE("15_TabAdd_InputValue_Set Dead_100"),
            OBFUSCATE("16_TabAdd_Toggle_Player Invisible"),
            OBFUSCATE("17_TabAdd_Toggle_Freeze Camera"),
            OBFUSCATE("18_TabAdd_SeekBar_Player Speed_0_100"),
            OBFUSCATE("19_TabAdd_SeekBar_Player Size_10_100"),
            OBFUSCATE("Tab_Bots Mods"),
            OBFUSCATE("20_TabAdd_Toggle_Kill bot"),
            OBFUSCATE("21_TabAdd_Toggle_No Shot"),
            OBFUSCATE("22_TabAdd_SeekBar_built Range_1_100"),
            OBFUSCATE("23_TabAdd_SeekBar_Bot Size_10_100"),
            OBFUSCATE("Tab_Tests"),
            OBFUSCATE("100_TabAdd_Button_Fire"),
            
    };

    int Total_Feature = (sizeof features / sizeof features[0]);
    ret = (jobjectArray)
            env->NewObjectArray(Total_Feature, env->FindClass(OBFUSCATE("java/lang/String")),
                                env->NewStringUTF(""));

    for (int i = 0; i < Total_Feature; i++)
        env->SetObjectArrayElement(ret, i, env->NewStringUTF(features[i]));

    return (ret);
}


void Changes(JNIEnv *env, jclass clazz, jobject obj, jint featNum, jstring featName, jint value, jlong Lvalue, jboolean boolean, jstring text) {

    switch (featNum) {
		case 11:
			if (boolean) {
			    if (PlayerInstance != NULL) SetHP(PlayerInstance, 9999999);
			} else {
			    if (PlayerInstance != NULL) SetHP(PlayerInstance, 100);
			}
			break;
		case 13:
		    FastRespawn = boolean;
		    break;
		case 14:
	        if (value >= 1) KillsValue = value;
	        break;
	    case 15:
	        if (value >= 1) deadsValue = value;
	        break;
		case 18:
		    if (value >= 1) PlayerSpeedValue = value;
		    break;
		case 100:
		    org_WeaponShoot(GadgetInstance);
		    break;
    }
}









// we will run our hacks in a new thread so our while loop doesn't block process main thread
void *hack_thread(void *) {
    LOGI(OBFUSCATE("pthread created"));

    //Check if target lib is loaded
    do {
        sleep(1);
    } while (!isLibraryLoaded(targetLibName));

    do {
        sleep(1);
        // getElfBaseMap can also find lib base even if it was loaded from zipped base.apk
        g_il2cppELF = ElfScanner::createWithPath(targetLibName);
    } while (!g_il2cppELF.isValid());

    LOGI(OBFUSCATE("%s has been loaded"), (const char *) targetLibName);

#if defined(__aarch64__)
    uintptr_t il2cppBase = g_il2cppELF.base();

    InstallHooks();
#elif defined(__arm__)


#endif

    LOGI(OBFUSCATE("Done"));
    return nullptr;
}

__attribute__((constructor))
void lib_main() {
    // Create a new thread so it does not block the main thread, means the game would not freeze
    pthread_t ptid;
    pthread_create(&ptid, NULL, hack_thread, NULL);
}
