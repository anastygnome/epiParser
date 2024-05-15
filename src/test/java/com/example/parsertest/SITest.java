package com.example.parsertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.Test;

class SITest {

    @Test
    void testGetEpigraphe() throws Exception {

        DateTimeFormatter DATE_FORMATTER = SI.XMLHandler.getDateFormatter();
        Epigraphe f = SI.getEpigraphe(59);

        assertNotNull(f);
        assertEquals(59, f.getId());
        assertEquals("étude Philippe Leveau", f.getName());
        assertEquals("Aux dieux Mânes. Lucius Satrius Maurus, a vécu plus ou moins 26 ans. Baebius Saturninus…",
                f.getTranslation());
        assertEquals(List.of("http://ccj-epicherchel.huma-num.fr/interface/phototheque/59/112991.jpg"), f.getImgUrl());
        assertEquals(LocalDate.parse("2018-07-12", DATE_FORMATTER), f.getDate());
        assertEquals("D ❦ M ❦L•SATRIVSMAVRVSVICSITANNIS•PLVSMINVSXXVI ❦ BAEBIVSSATVRNI------", f.getOriginal());
    }
}
