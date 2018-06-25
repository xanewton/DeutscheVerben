Deutsche Verben
=====================

<a href='https://play.google.com/store/apps/details?id=com.xengar.android.deutscheverben'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height=90px/></a>

![Scheme](/readmeImages/Screenshot_1529622638.png)
![Scheme](/readmeImages/Screenshot_1529622656.png)
![Scheme](/readmeImages/Screenshot_1529622670.png)


Android application to learn german verb tenses.


Pre-requisites
--------------
- Android SDK 25 or Higher
- [Color Picker Module](http://www.materialdoc.com/color-picker/)


References
----------
- https://en.wikipedia.org/wiki/German_verbs
- http://canoo.net/inflection/laufen:V:sein:haben
- http://conjugator.reverso.net/index-german-251-500.html  Conjugator
- https://en.wikipedia.org/wiki/German_verbs  Conjugator
- http://www.die-konjugation.de/verb/interpretieren.php Conjugator pattern
- http://conjugator.reverso.net/conjugation-german-verb-interpretieren.html Conjugator pattern

- https://www.pinterest.ca/pin/234187249353769621  top25
- http://www.thegermanprofessor.com/top-100-german-verbs/ top100 (in order)
- http://www.learnalanguage.com/learn-german/german-verbs/   top100, top500
- https://quizlet.com/15319894/250-german-verbs-mit-mnemonic-pt-1-flash-cards/   top250
- https://quizlet.com/6414/500-german-verbs-flash-cards/  top500
- https://zodml.org/sites/default/files/501_German_Verbs.pdf  top500 book, conjugation rules
- https://www.kobo.com/gb/en/ebook/501-german-verbs-5th-edition   top500

- https://www.linguee.com/german-english/translation/sein.html  Samples
- https://en.pons.com/translate?q=Sein&l=deen&in=ac_de&lf=de  Samples
- https://en.pons.com/translate/german-spanish/k%C3%B6nnen  Translations
- http://www.dict.cc/dict/options.php Samples

- http://www.canoo.net/services/Controller?service=canooNet&input=sein  Dictionary
- http://de.thefreedictionary.com/sein  Dictionary
- https://www.dwds.de/wb/kaufen  Dictionary
- https://en.pons.com/translate?q=kaufen&l=dedx&in=&lf= Dictionary



# Set up

Color Picker Module
-------------------

1.  Download repository from
  ```
  git clone https://android.googlesource.com/platform/frameworks/opt/colorpicker  (preferred) or
  git clone https://xengar@bitbucket.org/xengar/colorpicker.git
  ```

2. Import a new module in android studio with the New/import module menu,
   choosing the path where the project was cloned.
   Remove the empty "colorpicker" directory if needed.

3. Add dependency to app/build.gradle
   ```
   apply plugin: 'com.android.application'

   android {
       ...
   }

   dependencies {
       compile project(':colorpicker')
       ...
   }
   ```

4. Add compileSdkVersion and buildToolsVersion in colorpicker/build.gradle to avoid
   Error buildToolsVersion is not specified. Try to use latest versions.
   ```
    apply plugin: 'com.android.library'

    android {

        compileSdkVersion 26
        buildToolsVersion "26.0.1"

        sourceSets.main {
            manifest.srcFile 'AndroidManifest.xml'
            java.srcDirs = ['src']
            res.srcDirs = ['res']
        }
    }
   ```

5. Commit the modified changes in the colorpicker module.
   (There is no remote repository to push. Keep it local.)
   ```
   cd deutscheverben/colorpicker
   git add -A
   git commit -m "Import colorpicker module into Deutsche Verben project"
   ```

## License

Copyright 2018 Angel Garcia

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


