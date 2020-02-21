/*
 * Copyright (C) 2018 Angel Newton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xengar.android.deutscheverben.data

/**
 * Data that is contained for a conjugation.
 */
class Conjugation
/*** Constructor  */
(id: Long, termination: String, radicals: String,
 infinitivPrasens: String,
 infinitivPerfekt: String,
 partizipPrasens: String,
 partizipPerfekt: String,
 
 imperativDu: String,
 imperativIhr: String,
 imperativSie: String,

 indikativPrasensIch: String,
 indikativPrasensDu: String,
 indikativPrasensEr: String,
 indikativPrasensWir: String,
 indikativPrasensIhr: String,
 indikativPrasensSie: String,

 indikativPrateritumIch: String,
 indikativPrateritumDu: String,
 indikativPrateritumEr: String,
 indikativPrateritumWir: String,
 indikativPrateritumIhr: String,
 indikativPrateritumSie: String,

 indikativPerfektIch: String,
 indikativPerfektDu: String,
 indikativPerfektEr: String,
 indikativPerfektWir: String,
 indikativPerfektIhr: String,
 indikativPerfektSie: String,

 indikativPlusquamperfektIch: String,
 indikativPlusquamperfektDu: String,
 indikativPlusquamperfektEr: String,
 indikativPlusquamperfektWir: String,
 indikativPlusquamperfektIhr: String,
 indikativPlusquamperfektSie: String,

 indikativFutur1Ich: String,
 indikativFutur1Du: String,
 indikativFutur1Er: String,
 indikativFutur1Wir: String,
 indikativFutur1Ihr: String,
 indikativFutur1Sie: String,

 indikativFutur2Ich: String,
 indikativFutur2Du: String,
 indikativFutur2Er: String,
 indikativFutur2Wir: String,
 indikativFutur2Ihr: String,
 indikativFutur2Sie: String,

 konjunktiv1PrasensIch: String,
 konjunktiv1PrasensDu: String,
 konjunktiv1PrasensEr: String,
 konjunktiv1PrasensWir: String,
 konjunktiv1PrasensIhr: String,
 konjunktiv1PrasensSie: String,

 konjunktiv1PerfektIch: String,
 konjunktiv1PerfektDu: String,
 konjunktiv1PerfektEr: String,
 konjunktiv1PerfektWir: String,
 konjunktiv1PerfektIhr: String,
 konjunktiv1PerfektSie: String,

 konjunktiv1Futur1Ich: String,
 konjunktiv1Futur1Du: String,
 konjunktiv1Futur1Er: String,
 konjunktiv1Futur1Wir: String,
 konjunktiv1Futur1Ihr: String,
 konjunktiv1Futur1Sie: String,

 konjunktiv1Futur2Ich: String,
 konjunktiv1Futur2Du: String,
 konjunktiv1Futur2Er: String,
 konjunktiv1Futur2Wir: String,
 konjunktiv1Futur2Ihr: String,
 konjunktiv1Futur2Sie: String,

 konjunktiv2PrateritumIch: String,
 konjunktiv2PrateritumDu: String,
 konjunktiv2PrateritumEr: String,
 konjunktiv2PrateritumWir: String,
 konjunktiv2PrateritumIhr: String,
 konjunktiv2PrateritumSie: String,

 konjunktiv2PlusquamperfektIch: String,
 konjunktiv2PlusquamperfektDu: String,
 konjunktiv2PlusquamperfektEr: String,
 konjunktiv2PlusquamperfektWir: String,
 konjunktiv2PlusquamperfektIhr: String,
 konjunktiv2PlusquamperfektSie: String,

 konjunktiv2Futur1Ich: String,
 konjunktiv2Futur1Du: String,
 konjunktiv2Futur1Er: String,
 konjunktiv2Futur1Wir: String,
 konjunktiv2Futur1Ihr: String,
 konjunktiv2Futur1Sie: String,

 konjunktiv2Futur2Ich: String,
 konjunktiv2Futur2Du: String,
 konjunktiv2Futur2Er: String,
 konjunktiv2Futur2Wir: String,
 konjunktiv2Futur2Ihr: String,
 konjunktiv2Futur2Sie: String) {

    /* Getters and Setters */
    var id: Long = 0
    var termination = ""
    var radicals = ""

    var infinitivPrasens = ""
    var infinitivPerfekt = ""
    var partizipPrasens = ""
    var partizipPerfekt = ""

    var imperativDu = ""
    var imperativIhr = ""
    var imperativSie = ""

    var indikativPrasensIch = ""
    var indikativPrasensDu = ""
    var indikativPrasensEr = ""
    var indikativPrasensWir = ""
    var indikativPrasensIhr = ""
    var indikativPrasensSie = ""

    var indikativPrateritumIch = ""
    var indikativPrateritumDu = ""
    var indikativPrateritumEr = ""
    var indikativPrateritumWir = ""
    var indikativPrateritumIhr = ""
    var indikativPrateritumSie = ""

    var indikativPerfektIch = ""
    var indikativPerfektDu = ""
    var indikativPerfektEr = ""
    var indikativPerfektWir = ""
    var indikativPerfektIhr = ""
    var indikativPerfektSie = ""

    var indikativPlusquamperfektIch = ""
    var indikativPlusquamperfektDu = ""
    var indikativPlusquamperfektEr = ""
    var indikativPlusquamperfektWir = ""
    var indikativPlusquamperfektIhr = ""
    var indikativPlusquamperfektSie = ""

    var indikativFutur1Ich = ""
    var indikativFutur1Du = ""
    var indikativFutur1Er = ""
    var indikativFutur1Wir = ""
    var indikativFutur1Ihr = ""
    var indikativFutur1Sie = ""

    var indikativFutur2Ich = ""
    var indikativFutur2Du = ""
    var indikativFutur2Er = ""
    var indikativFutur2Wir = ""
    var indikativFutur2Ihr = ""
    var indikativFutur2Sie = ""

    var konjunktiv1PrasensIch = ""
    var konjunktiv1PrasensDu = ""
    var konjunktiv1PrasensEr = ""
    var konjunktiv1PrasensWir = ""
    var konjunktiv1PrasensIhr = ""
    var konjunktiv1PrasensSie = ""

    var konjunktiv1PerfektIch = ""
    var konjunktiv1PerfektDu = ""
    var konjunktiv1PerfektEr = ""
    var konjunktiv1PerfektWir = ""
    var konjunktiv1PerfektIhr = ""
    var konjunktiv1PerfektSie = ""

    var konjunktiv1Futur1Ich = ""
    var konjunktiv1Futur1Du = ""
    var konjunktiv1Futur1Er = ""
    var konjunktiv1Futur1Wir = ""
    var konjunktiv1Futur1Ihr = ""
    var konjunktiv1Futur1Sie = ""

    var konjunktiv1Futur2Ich = ""
    var konjunktiv1Futur2Du = ""
    var konjunktiv1Futur2Er = ""
    var konjunktiv1Futur2Wir = ""
    var konjunktiv1Futur2Ihr = ""
    var konjunktiv1Futur2Sie = ""

    var konjunktiv2PrateritumIch = ""
    var konjunktiv2PrateritumDu = ""
    var konjunktiv2PrateritumEr = ""
    var konjunktiv2PrateritumWir = ""
    var konjunktiv2PrateritumIhr = ""
    var konjunktiv2PrateritumSie = ""

    var konjunktiv2PlusquamperfektIch = ""
    var konjunktiv2PlusquamperfektDu = ""
    var konjunktiv2PlusquamperfektEr = ""
    var konjunktiv2PlusquamperfektWir = ""
    var konjunktiv2PlusquamperfektIhr = ""
    var konjunktiv2PlusquamperfektSie = ""

    var konjunktiv2Futur1Ich = ""
    var konjunktiv2Futur1Du = ""
    var konjunktiv2Futur1Er = ""
    var konjunktiv2Futur1Wir = ""
    var konjunktiv2Futur1Ihr = ""
    var konjunktiv2Futur1Sie = ""

    var konjunktiv2Futur2Ich = ""
    var konjunktiv2Futur2Du = ""
    var konjunktiv2Futur2Er = ""
    var konjunktiv2Futur2Wir = ""
    var konjunktiv2Futur2Ihr = ""
    var konjunktiv2Futur2Sie = ""

    init {
        this.id = id
        this.termination = termination
        this.radicals = radicals

        this.infinitivPrasens = infinitivPrasens
        this.infinitivPerfekt = infinitivPerfekt
        this.partizipPrasens = partizipPrasens
        this.partizipPerfekt = partizipPerfekt

        this.imperativDu = imperativDu
        this.imperativIhr = imperativIhr
        this.imperativSie = imperativSie

        this.indikativPrasensIch = indikativPrasensIch
        this.indikativPrasensDu = indikativPrasensDu
        this.indikativPrasensEr = indikativPrasensEr
        this.indikativPrasensWir = indikativPrasensWir
        this.indikativPrasensIhr = indikativPrasensIhr
        this.indikativPrasensSie = indikativPrasensSie

        this.indikativPrateritumIch = indikativPrateritumIch
        this.indikativPrateritumDu = indikativPrateritumDu
        this.indikativPrateritumEr = indikativPrateritumEr
        this.indikativPrateritumWir = indikativPrateritumWir
        this.indikativPrateritumIhr = indikativPrateritumIhr
        this.indikativPrateritumSie = indikativPrateritumSie

        this.indikativPerfektIch = indikativPerfektIch
        this.indikativPerfektDu = indikativPerfektDu
        this.indikativPerfektEr = indikativPerfektEr
        this.indikativPerfektWir = indikativPerfektWir
        this.indikativPerfektIhr = indikativPerfektIhr
        this.indikativPerfektSie = indikativPerfektSie

        this.indikativPlusquamperfektIch = indikativPlusquamperfektIch
        this.indikativPlusquamperfektDu = indikativPlusquamperfektDu
        this.indikativPlusquamperfektEr = indikativPlusquamperfektEr
        this.indikativPlusquamperfektWir = indikativPlusquamperfektWir
        this.indikativPlusquamperfektIhr = indikativPlusquamperfektIhr
        this.indikativPlusquamperfektSie = indikativPlusquamperfektSie

        this.indikativFutur1Ich = indikativFutur1Ich
        this.indikativFutur1Du = indikativFutur1Du
        this.indikativFutur1Er = indikativFutur1Er
        this.indikativFutur1Wir = indikativFutur1Wir
        this.indikativFutur1Ihr = indikativFutur1Ihr
        this.indikativFutur1Sie = indikativFutur1Sie

        this.indikativFutur2Ich = indikativFutur2Ich
        this.indikativFutur2Du = indikativFutur2Du
        this.indikativFutur2Er = indikativFutur2Er
        this.indikativFutur2Wir = indikativFutur2Wir
        this.indikativFutur2Ihr = indikativFutur2Ihr
        this.indikativFutur2Sie = indikativFutur2Sie

        this.konjunktiv1PrasensIch = konjunktiv1PrasensIch
        this.konjunktiv1PrasensDu = konjunktiv1PrasensDu
        this.konjunktiv1PrasensEr = konjunktiv1PrasensEr
        this.konjunktiv1PrasensWir = konjunktiv1PrasensWir
        this.konjunktiv1PrasensIhr = konjunktiv1PrasensIhr
        this.konjunktiv1PrasensSie = konjunktiv1PrasensSie

        this.konjunktiv1PerfektIch = konjunktiv1PerfektIch
        this.konjunktiv1PerfektDu = konjunktiv1PerfektDu
        this.konjunktiv1PerfektEr = konjunktiv1PerfektEr
        this.konjunktiv1PerfektWir = konjunktiv1PerfektWir
        this.konjunktiv1PerfektIhr = konjunktiv1PerfektIhr
        this.konjunktiv1PerfektSie = konjunktiv1PerfektSie

        this.konjunktiv1Futur1Ich = konjunktiv1Futur1Ich
        this.konjunktiv1Futur1Du = konjunktiv1Futur1Du
        this.konjunktiv1Futur1Er = konjunktiv1Futur1Er
        this.konjunktiv1Futur1Wir = konjunktiv1Futur1Wir
        this.konjunktiv1Futur1Ihr = konjunktiv1Futur1Ihr
        this.konjunktiv1Futur1Sie = konjunktiv1Futur1Sie

        this.konjunktiv1Futur2Ich = konjunktiv1Futur2Ich
        this.konjunktiv1Futur2Du = konjunktiv1Futur2Du
        this.konjunktiv1Futur2Er = konjunktiv1Futur2Er
        this.konjunktiv1Futur2Wir = konjunktiv1Futur2Wir
        this.konjunktiv1Futur2Ihr = konjunktiv1Futur2Ihr
        this.konjunktiv1Futur2Sie = konjunktiv1Futur2Sie

        this.konjunktiv2PrateritumIch = konjunktiv2PrateritumIch
        this.konjunktiv2PrateritumDu = konjunktiv2PrateritumDu
        this.konjunktiv2PrateritumEr = konjunktiv2PrateritumEr
        this.konjunktiv2PrateritumWir = konjunktiv2PrateritumWir
        this.konjunktiv2PrateritumIhr = konjunktiv2PrateritumIhr
        this.konjunktiv2PrateritumSie = konjunktiv2PrateritumSie

        this.konjunktiv2PlusquamperfektIch = konjunktiv2PlusquamperfektIch
        this.konjunktiv2PlusquamperfektDu = konjunktiv2PlusquamperfektDu
        this.konjunktiv2PlusquamperfektEr = konjunktiv2PlusquamperfektEr
        this.konjunktiv2PlusquamperfektWir = konjunktiv2PlusquamperfektWir
        this.konjunktiv2PlusquamperfektIhr = konjunktiv2PlusquamperfektIhr
        this.konjunktiv2PlusquamperfektSie = konjunktiv2PlusquamperfektSie

        this.konjunktiv2Futur1Ich = konjunktiv2Futur1Ich
        this.konjunktiv2Futur1Du = konjunktiv2Futur1Du
        this.konjunktiv2Futur1Er = konjunktiv2Futur1Er
        this.konjunktiv2Futur1Wir = konjunktiv2Futur1Wir
        this.konjunktiv2Futur1Ihr = konjunktiv2Futur1Ihr
        this.konjunktiv2Futur1Sie = konjunktiv2Futur1Sie

        this.konjunktiv2Futur2Ich = konjunktiv2Futur2Ich
        this.konjunktiv2Futur2Du = konjunktiv2Futur2Du
        this.konjunktiv2Futur2Er = konjunktiv2Futur2Er
        this.konjunktiv2Futur2Wir = konjunktiv2Futur2Wir
        this.konjunktiv2Futur2Ihr = konjunktiv2Futur2Ihr
        this.konjunktiv2Futur2Sie = konjunktiv2Futur2Sie
    }

}
