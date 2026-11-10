package mines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import javax.swing.JLabel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BoardTest {

    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    private int[] grille(Board b) throws Exception {
        Field f = Board.class.getDeclaredField("field");
        f.setAccessible(true);
        return (int[]) f.get(b);
    }

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
}
