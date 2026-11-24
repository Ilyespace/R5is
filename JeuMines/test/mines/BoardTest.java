package mines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import javax.swing.JLabel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BoardTest {

    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    // ---------- outils ----------

    private Field champ(String nom) throws Exception {
        Field f = Board.class.getDeclaredField(nom);
        f.setAccessible(true);
        return f;
    }

    private int[] grille(Board b) throws Exception {
        return (int[]) champ("field").get(b);
    }

    private void fixerMinesRestantes(Board b, int valeur) throws Exception {
        champ("mines_left").setInt(b, valeur);
    }

    private boolean enJeu(Board b) throws Exception {
        return champ("inGame").getBoolean(b);
    }

    private void fixerEnJeu(Board b, boolean valeur) throws Exception {
        champ("inGame").setBoolean(b, valeur);
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

    private void toutCouvertVide(int[] g) {
        for (int i = 0; i < g.length; i++) {
            g[i] = 10;
        }
    }

    // ---------- tests de depart ----------

    @Test
    void newGamePlace40Mines() throws Exception {
        Board b = new Board(new JLabel());
        int mines = 0;
        for (int c : grille(b)) {
            if (c == 19) mines++;
        }
        assertEquals(40, mines);
    }

    @Test
    void toutesLesCasesSontCouvertes() throws Exception {
        Board b = new Board(new JLabel());
        for (int c : grille(b)) {
            assertTrue(c >= 10);
        }
    }

    @Test
    void laBarreDetatAfficheLesMinesRestantes() {
        JLabel label = new JLabel();
        new Board(label);
        assertEquals("40", label.getText());
    }

    // ---------- generation de la grille ----------

    @Test
    void newGameRepeteGardeUneGrilleValide() throws Exception {
        Board b = new Board(new JLabel());
        for (int partie = 0; partie < 200; partie++) {
            b.newGame();
            int mines = 0;
            for (int c : grille(b)) {
                assertTrue(c >= 10 && c <= 19);
                if (c == 19) mines++;
            }
            assertEquals(40, mines);
        }
    }

    // ---------- affichage (paint) ----------

    @Test
    void paintPendantLaPartieNeChangePasLEtat() throws Exception {
        JLabel label = new JLabel();
        Board b = new Board(label);
        grille(b)[0] = 29;
        grille(b)[1] = 25;
        dessiner(b);
        assertTrue(enJeu(b));
        assertEquals("40", label.getText());
    }

    @Test
    void paintDetecteLaVictoire() throws Exception {
        JLabel label = new JLabel();
        Board b = new Board(label);
        int[] g = grille(b);
        for (int i = 0; i < g.length; i++) {
            g[i] = 0;
        }
        dessiner(b);
        assertEquals("Game won", label.getText());
        assertFalse(enJeu(b));
    }

    @Test
    void paintDetecteLaDefaiteEtMontreLesMines() throws Exception {
        JLabel label = new JLabel();
        Board b = new Board(label);
        int[] g = grille(b);
        toutCouvertVide(g);
        g[0] = 9;
        g[1] = 19;
        g[2] = 29;
        g[3] = 25;
        g[4] = 12;
        dessiner(b);
        assertEquals("Game lost", label.getText());
        assertFalse(enJeu(b));
    }

    // ---------- clics de souris ----------

    @Test
    void clicDroitPoseEtRetireUnDrapeau() throws Exception {
        JLabel label = new JLabel();
        Board b = new Board(label);
        int[] g = grille(b);
        g[0] = 10;
        clic(b, 1, 1, MouseEvent.BUTTON3);
        assertEquals(20, g[0]);
        assertEquals("39", label.getText());
        clic(b, 1, 1, MouseEvent.BUTTON3);
        assertEquals(10, g[0]);
        assertEquals("40", label.getText());
    }

    @Test
    void clicDroitSansDrapeauRestant() throws Exception {
        JLabel label = new JLabel();
        Board b = new Board(label);
        int[] g = grille(b);
        g[0] = 10;
        fixerMinesRestantes(b, 0);
        clic(b, 1, 1, MouseEvent.BUTTON3);
        assertEquals(10, g[0]);
        assertEquals("No marks left", label.getText());
    }

    @Test
    void clicDroitSurCaseDecouverteNeFaitRien() throws Exception {
        Board b = new Board(new JLabel());
        int[] g = grille(b);
        g[0] = 1;
        clic(b, 1, 1, MouseEvent.BUTTON3);
        assertEquals(1, g[0]);
    }

    @Test
    void clicGaucheDecouvreUneCase() throws Exception {
        Board b = new Board(new JLabel());
        int[] g = grille(b);
        g[0] = 11;
        clic(b, 1, 1, MouseEvent.BUTTON1);
        assertEquals(1, g[0]);
        assertTrue(enJeu(b));
    }

    @Test
    void clicGaucheSurUneMineTermineLaPartie() throws Exception {
        Board b = new Board(new JLabel());
        int[] g = grille(b);
        g[0] = 19;
        clic(b, 1, 1, MouseEvent.BUTTON1);
        assertEquals(9, g[0]);
        assertFalse(enJeu(b));
    }

    @Test
    void clicGaucheSurCaseVideDecouvreLesCasesVoisines() throws Exception {
        Board b = new Board(new JLabel());
        int[] g = grille(b);
        toutCouvertVide(g);
        clic(b, 1, 1, MouseEvent.BUTTON1);
        for (int c : g) {
            assertEquals(0, c);
        }
    }

    @Test
    void clicGaucheSurUnDrapeauNeFaitRien() throws Exception {
        Board b = new Board(new JLabel());
        int[] g = grille(b);
        g[0] = 20;
        clic(b, 1, 1, MouseEvent.BUTTON1);
        assertEquals(20, g[0]);
    }

    @Test
    void clicApresLaFinDeLaPartieRelanceUneNouvellePartie() throws Exception {
        JLabel label = new JLabel();
        Board b = new Board(label);
        fixerEnJeu(b, false);
        fixerMinesRestantes(b, 3);
        clic(b, 1, 1, MouseEvent.BUTTON3);
        // newGame remet 40 mines restantes, puis le clic droit pose un drapeau : 39
        assertEquals("39", label.getText());
    }

    @Test
    void clicEnDehorsDeLaGrilleNeFaitRien() throws Exception {
        Board b = new Board(new JLabel());
        int[] g = grille(b);
        int[] avant = g.clone();
        clic(b, 500, 500, MouseEvent.BUTTON1);
        for (int i = 0; i < g.length; i++) {
            assertEquals(avant[i], g[i]);
        }
    }
}