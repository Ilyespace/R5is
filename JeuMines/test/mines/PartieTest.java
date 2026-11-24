package mines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class PartieTest {

    @Test
    void refuseUnNombreDeJoueursInvalide() {
        assertThrows(IllegalArgumentException.class, () -> new Partie(1));
        assertThrows(IllegalArgumentException.class, () -> new Partie(5));
    }

    @Test
    void commenceAvecLeJoueur1() {
        Partie p = new Partie(3);
        assertEquals(3, p.getNombreJoueurs());
        assertEquals(0, p.getJoueurCourant());
        assertEquals(0, p.getScore(2));
        assertEquals(-1, p.getPerdant());
        assertFalse(p.estTerminee());
        assertTrue(p.gagnants().isEmpty());
    }

    @Test
    void lesJoueursJouentChacunSonTour() {
        Partie p = new Partie(3);
        p.enregistrerCoup(2, false, false);
        assertEquals(1, p.getJoueurCourant());
        p.enregistrerCoup(5, false, false);
        assertEquals(2, p.getJoueurCourant());
        p.enregistrerCoup(1, false, false);
        assertEquals(0, p.getJoueurCourant());
        p.enregistrerCoup(4, false, false);
        assertEquals(6, p.getScore(0));
        assertEquals(5, p.getScore(1));
        assertEquals(1, p.getScore(2));
    }

    @Test
    void celuiQuiToucheUneMinePerdEtLAutreGagne() {
        Partie p = new Partie(2);
        p.enregistrerCoup(4, false, false);
        p.enregistrerCoup(0, true, false);
        assertTrue(p.estTerminee());
        assertEquals(1, p.getPerdant());
        assertEquals(Arrays.asList(0), p.gagnants());
    }

    @Test
    void entreLesNonPerdantsLeMeilleurScoreGagne() {
        Partie p = new Partie(3);
        p.enregistrerCoup(3, false, false);
        p.enregistrerCoup(7, false, false);
        p.enregistrerCoup(0, true, false);
        assertEquals(2, p.getPerdant());
        assertEquals(Arrays.asList(1), p.gagnants());
    }

    @Test
    void egaliteEntreLesNonPerdants() {
        Partie p = new Partie(3);
        p.enregistrerCoup(3, false, false);
        p.enregistrerCoup(3, false, false);
        p.enregistrerCoup(0, true, false);
        assertEquals(Arrays.asList(0, 1), p.gagnants());
    }

    @Test
    void toutDecouvertSansMineLeMeilleurScoreGagne() {
        Partie p = new Partie(2);
        p.enregistrerCoup(10, false, false);
        p.enregistrerCoup(30, false, true);
        assertTrue(p.estTerminee());
        assertEquals(-1, p.getPerdant());
        assertEquals(Arrays.asList(1), p.gagnants());
    }

    @Test
    void toutDecouvertAvecScoresEgaux() {
        Partie p = new Partie(2);
        p.enregistrerCoup(5, false, false);
        p.enregistrerCoup(5, false, true);
        assertEquals(Arrays.asList(0, 1), p.gagnants());
    }

    @Test
    void lesCoupsApresLaFinSontIgnores() {
        Partie p = new Partie(2);
        p.enregistrerCoup(0, true, false);
        p.enregistrerCoup(10, false, false);
        assertEquals(0, p.getScore(0));
        assertEquals(0, p.getScore(1));
        assertEquals(0, p.getJoueurCourant());
    }

    @Test
    void reinitialiserRemetToutAZero() {
        Partie p = new Partie(2);
        p.enregistrerCoup(5, false, false);
        p.enregistrerCoup(0, true, false);
        p.reinitialiser();
        assertFalse(p.estTerminee());
        assertEquals(0, p.getJoueurCourant());
        assertEquals(0, p.getScore(0));
        assertEquals(-1, p.getPerdant());
    }

    @Test
    void resumePendantLaPartie() {
        Partie p = new Partie(2);
        p.enregistrerCoup(5, false, false);
        assertEquals("Tour J2 | J1:5 J2:0 | Mines 38", p.resume(38));
    }

    @Test
    void resumeApresUneMine() {
        Partie p = new Partie(2);
        p.enregistrerCoup(5, false, false);
        p.enregistrerCoup(0, true, false);
        assertEquals("J2 saute sur une mine | Gagnant : J1", p.resume(40));
    }

    @Test
    void resumeApresUneVictoireAvecEgalite() {
        Partie p = new Partie(2);
        p.enregistrerCoup(5, false, false);
        p.enregistrerCoup(5, false, true);
        assertEquals("Fin | Egalite : J1, J2 | J1:5 J2:5", p.resume(0));
    }
}
