package physics2d.rigidbody;

import org.joml.Vector2f;
import org.joml.Vector4f;
import physics2d.primitives.AABB;
import physics2d.primitives.Box2D;
import physics2d.primitives.Circle;
import renderer.Line2D;
import util.KappaMath;

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

        public static boolean pointInBox2D (Vector2f point, Box2D box){
                //Translate the point into local space
                Vector2f pointLocalBoxSpace = new Vector2f();
                KappaMath.rotate(pointLocalBoxSpace, box.getRigidbody().getRotation(), box.getRigidbody().getPosition());

                Vector2f min = box.getMin();
                Vector2f max = box.getMax();

                return pointLocalBoxSpace.x <= max.x && min.x <= pointLocalBoxSpace.x && pointLocalBoxSpace.y <= max.y && min.y <= pointLocalBoxSpace.y;
        }

        public static boolean lineAndCircle (Line2D line, Circle circle){
                if(pointInCircle(line.getStart(), circle) || pointInCircle(line.getEnd(), circle)){
                        return true;
                }

                Vector2f ab = new Vector2f(line.getEnd().sub(line.getStart()));

                //Project point (circle position) onto ab (line segment)
                Vector2f circleCentre = circle.getCentre();
                Vector2f centreToLineStart = new Vector2f(circleCentre).sub(line.getStart());
                float t = centreToLineStart.dot(ab) / ab.dot(ab);

                if(t < 0.0f || t > 1.0f){
                        return false;
                }

                //Find the closest point to the line segment
                Vector2f closestPoint = new Vector2f(line.getStart().add(ab.mul(t)));

                return pointInCircle(closestPoint, circle);
        }
}
