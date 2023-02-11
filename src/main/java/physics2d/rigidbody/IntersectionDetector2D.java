package physics2d.rigidbody;

import org.joml.Vector2f;
import physics2d.primitives.AABB;
import physics2d.primitives.Box2D;
import physics2d.primitives.Circle;
import renderer.Line2D;

public class IntersectionDetector2D {
        public static boolean pointOnLine(Vector2f point, Line2D line){
                float dy = line.getEnd().y - line.getStart().y;
                float dx = line.getEnd().x - line.getStart().x;
                float m = dy / dx;

                float slope = line.getEnd().y - (m * line.getEnd().x);

                //Check the line equation
                return point.y == m * point.x + slope;
        }

        public static boolean pointInCircle(Vector2f point, Circle circle){
                Vector2f circleCentre = circle.getCentre();
                Vector2f centreToPoint = new Vector2f(point).sub(circleCentre);

                return centreToPoint.lengthSquared() <= circle.getRadius() * circle.getRadius();
        }

        public static boolean pointInAABB(Vector2f point, AABB box){
                Vector2f min = box.getMin();
                Vector2f max = box.getMax();

                return point.x <= max.x && min.x <= point.x && point.y <= max.y && min.y <= point.y;
        }

        public static boolean pointInBox2D (Vector2f, Box2D box){

        }
}
