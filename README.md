# jni-helper

## 测试库

### example.cpp

```cpp
#include "jni.h"
#include <iostream>

using namespace std;

#ifdef __cplusplus
extern "C" {
#endif

  JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_8);
    jclass clz(env->FindClass("java/lang/System"));
    jobject obj(env->CallStaticObjectMethod(clz, env->GetStaticMethodID(clz, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;"), env->NewStringUTF("jni.config")));
    cout << env->GetStringUTFChars(static_cast<jstring>(obj), JNI_FALSE) << endl;
    return JNI_VERSION_1_8;
  }

#ifdef __cplusplus
}
#endif
```

### CMakeList.txt
```text
project ("Example")
add_library(${PROJECT_NAME} SHARED "example.cpp")
target_include_directories(${PROJECT_NAME} PUBLIC $ENV{JAVA_HOME}/include $ENV{JAVA_HOME}/include/win32 $ENV{JAVA_HOME}/include/linux)
target_compile_options(${PROJECT_NAME} PUBLIC "-fPIC")
```