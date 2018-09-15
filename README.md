# retrofit2-queue
The library supports retrofit2 request execution sequentially.

Kotlin version is <a href="https://github.com/hieupham1993/retrofit2-queue-kotlin">here</a>

## Installation
************* **Using Gradle** *************

Add repository:
```groovy
repositories {
    jcenter()
}
```
or
```groovy
repositories {
    maven {
        url "https://dl.bintray.com/hieupham1993/utilities" 
    }
}
```
Add this in your app's build.gradle file:

**For Gradle < 3.4**

```groovy
compile 'com.hieupt:retrofit2-queue:1.0.1'
```

**For Gradle >= 3.4**

```groovy
implementation 'com.hieupt:retrofit2-queue:1.0.1'
```
************* **Using Maven** *************
```xml
<dependency>
  <groupId>com.hieupt</groupId>
  <artifactId>retrofit2-queue</artifactId>
  <version>1.0.1</version>
  <type>pom</type>
</dependency>
```
## Usage
**Create RetrofitQueue instance**
```java
// new instance
RetrofitQueue retrofitQueue = new RetrofitQueue();
// singleton
RetrofitQueue retrofitQueue = RetrofitQueueSingleton.getInstance();
```
**Set number of request can be executed in parallel**
```java
retrofitQueue.updateMaxActiveRequest(int);
```
**Add request to queue**
```java
retrofitQueue.addRequest(Call, Callback);
// or
retrofitQueue.addRequestToFrontQueue(Call, Callback);
```
**Execute a request immediately**
```java
retrofitQueue.requestNow(Call, Callback);
```
**Cancel requests that executed but not finished yet**
```java
// cancel all executed request
retrofitQueue.cancel();
// or cancel specific request
retrofitQueue.cancel(Call);
```
**Remove waiting requests**
```java
// clear pending queue
retrofitQueue.clearQueue();
// or remove specific request
retrofitQueue.removeRequest(Call);
```
