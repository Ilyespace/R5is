package mines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Arrays;
import javax.swing.JLabel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BoardMultiTest {

    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    private Field champ(String nom) throws Exception {
        Field f = Board.class.getDeclaredField(nom);
        f.setAccessible(true);
        return f;
    }

    private int[] grille(Board b) throws Exception {
        return (int[]) champ("field").get(b);
    }

    private boolean enJeu(Board b) throws Exception {
        return champ("inGame").getBoolean(b);
    }

    private void dessiner(Board b) {
        BufferedImage image = new BufferedImage(240, 240, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        b.paint(g);
        g.dispose();
    }

    private void clic(Board b, int x, int y, int bouton) {
        MouseEvent e = new MouseEvent(b, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), 0, x, y, 1, false, bouton);
        for (MouseListener l : b.getMouseListeners()) {
            l.mousePressed(e);
        }
    }

    private Board nouvellePartie(JLabel label, int joueurs) {
        Board b = new Board(label);
        b.setPartie(new Partie(joueurs));
        return b;
    }

    @Test
    void sansPartieLeJeuResteEnModeSolo() {
        Board b = new Board(new JLabel());
        assertTrue(b.getPartie() == null);
    }

    @Test
    void laBarreDetatAnnonceLeTourDuJoueur1() {
        JLabel label = new JLabel();
        nouvellePartie(label, 2);
        assertEquals("Tour J1 | J1:0 J2:0 | Mines 40", label.getText());
    }

    @Test
    void unCoupFaitPasserAuJoueurSuivantEtRapporteUnPoint() throws Exception {
        Board b = nouvellePartie(new JLabel(), 2);
        int[] g = grille(b);
        g[0] = 11;
        clic(b, 1, 1, MouseEvent.BUTTON1);
        assertEquals(1, g[0]);
        assertEquals(1, b.getPartie().getScore(0));
        assertEquals(1, b.getPartie().getJoueurCourant());
    }

    @Test
    void toucherUneMineFaitPerdreLeJoueurCourant() throws Exception {
        JLabel label = new JLabel();
        Board b = nouvellePartie(label, 2);
        grille(b)[0] = 19;
        clic(b, 1, 1, MouseEvent.BUTTON1);
        assertTrue(b.getPartie().estTerminee());
        assertEquals(0, b.getPartie().getPerdant());
        assertEquals(Arrays.asList(1), b.getPartie().gagnants());
        assertFalse(enJeu(b));
        assertEquals("J1 saute sur une mine | Gagnant : J2", label.getText());
    }

    @Test
    void laBarreDetatResteApresLAffichageEnFinDePartie() throws Exception {
        JLabel label = new JLabel();
        Board b = nouvellePartie(label, 2);
        grille(b)[0] = 19;
        clic(b, 1, 1, MouseEvent.BUTTON1);
        dessiner(b);
        assertEquals("J1 saute sur une mine | Gagnant : J2", label.getText());
    }

    @Test
    void decouvrirToutesLesCasesSuresTermineLaPartieSansPerdant() throws Exception {
        Board b = nouvellePartie(new JLabel(), 2);
        int[] g = grille(b);
        for (int i = 0; i < g.length; i++) {
            g[i] = 10;
        }
        clic(b, 1, 1, MouseEvent.BUTTON1);
        assertTrue(b.getPartie().estTerminee());
        assertEquals(-1, b.getPartie().getPerdant());
        assertEquals(256, b.getPartie().getScore(0));
        assertEquals(Arrays.asList(0), b.getPartie().gagnants());
        assertFalse(enJeu(b));
    }

    @Test
    void unClicDroitNeChangePasLeTour() throws Exception {
        Board b = nouvellePartie(new JLabel(), 2);
        int[] g = grille(b);
        g[0] = 10;
        clic(b, 1, 1, MouseEvent.BUTTON3);
        assertEquals(20, g[0]);
        assertEquals(0, b.getPartie().getJoueurCourant());
    }

    @Test
    void cliquerSurUneCaseDejaDecouverteNeChangePasLeTour() throws Exception {
        Board b = nouvellePartie(new JLabel(), 2);
        grille(b)[0] = 1;
        clic(b, 1, 1, MouseEvent.BUTTON1);
        assertEquals(0, b.getPartie().getJoueurCourant());
        assertEquals(0, b.getPartie().getScore(0));
    }

    @Test
    void cliquerSurUnDrapeauNeChangePasLeTour() throws Exception {
        Board b = nouvellePartie(new JLabel(), 2);
        grille(b)[0] = 20;
        clic(b, 1, 1, MouseEvent.BUTTON1);
        assertEquals(0, b.getPartie().getJoueurCourant());
        assertEquals(20, grille(b)[0]);
    }

    @Test
    void unClicApresLaFinRelanceUneNouvellePartie() throws Exception {
        Board b = nouvellePartie(new JLabel(), 2);
        grille(b)[0] = 19;
        clic(b, 1, 1, MouseEvent.BUTTON1);
        assertTrue(b.getPartie().estTerminee());
        clic(b, 1, 1, MouseEvent.BUTTON3);
        assertFalse(b.getPartie().estTerminee());
        assertEquals(0, b.getPartie().getScore(0));
        assertEquals(0, b.getPartie().getJoueurCourant());
    }

    @Test
    void newGameRemetLesScoresAZero() {
        Board b = nouvellePartie(new JLabel(), 3);
        b.getPartie().enregistrerCoup(5, false, false);
        b.newGame();
        assertEquals(0, b.getPartie().getScore(0));
        assertEquals(0, b.getPartie().getJoueurCourant());
    }
}
