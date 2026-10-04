# Automated tests for [libdemo](https://github.com/Valkeru/libdemo) project API

Default testing target is the application image.  
Testing of locally launched application as available too with disabled `test-release` Maven profile.  
Check parameters in application.yml before testing local instance.  

## Requirements

+ Java 21 and above
+ Docker

## How to use

### CLI

```shell
./mvnw test
``` 
To test local instance:  
```shell
./mvnw test -P'!test-release'
```

### IDEA

+ Run "tests" target in maven tool window
+ Click "java" catalog with right mouse button in project tree and click "Run 'tests' in 'java'" option

Disable `test-release` in `Profiles` to test local instance
