package physics2d;

import org.joml.Vector2f;
import physics2d.rigidbody.IntersectionDetector2D;
import renderer.Line2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class collisionDetectorTest {
    private final float EPSILON = 0.000001f;
    @Test
    public void pointOnLine2DShouldReturnTrueTest(){
        Line2D line = new Line2D(new Vector2f(0, 0), new Vector2f(12, 4));
        Vector2f point = new Vector2f(0,0);

        assertTrue(IntersectionDetector2D.pointOnLine(point, line));
    }

    @Test
    public void pointOnLine2DShouldReturnTrueTestTwo(){
        Line2D line = new Line2D(new Vector2f(0, 0), new Vector2f(12, 4));
        Vector2f point = new Vector2f(12,4);

        assertTrue(IntersectionDetector2D.pointOnLine(point, line));
    }

    @Test
    public void pointOnLine2DShouldReturnTrueTestThree(){
        Line2D line = new Line2D(new Vector2f(0, 0), new Vector2f(0, 10));
        Vector2f point = new Vector2f(0,5);

        boolean result = IntersectionDetector2D.pointOnLine(point, line);
        assertTrue(result);
    }
}
