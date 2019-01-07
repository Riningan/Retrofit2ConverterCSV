# Retrofit2ConverterCSV

[ ![Download](https://api.bintray.com/packages/riningan/AndroidMaven/Retrofit2ConverterCSV/images/download.svg) ](https://bintray.com/riningan/AndroidMaven/Retrofit2ConverterCSV/_latestVersion)

Work via [gson converter](https://github.com/square/retrofit/tree/master/retrofit-converters/gson).

In response: csv will convert to json. 

In request: json will convert to csv if possible.

Using retrofit version is 2.4.0


USAGE
---

Using Retrofit2ConverterCSV in your application.
Add dependencies in build.gradle of your module.

```groovy
dependencies {
  implementation 'com.riningan.retrofit2:converter-csv:1.1'
}
```

```java
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("https://api.example.com")
    .addConverterFactory(CsvConverterFactory.create())
    .build();
```


LICENCE
-----

  	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	   http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
