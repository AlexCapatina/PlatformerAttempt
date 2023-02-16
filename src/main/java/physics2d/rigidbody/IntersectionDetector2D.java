package physics2d.rigidbody;

import org.joml.Vector2f;
import physics2d.primitives.*;
import renderer.Line2D;
import util.KappaMath;

public class IntersectionDetector2D {
        public static boolean pointOnLine(Vector2f point, Line2D line){
                float dy = line.getEnd().y - line.getStart().y;
                float dx = line.getEnd().x - line.getStart().x;
                if(dx == 0.0f){
                        return KappaMath.compare(point.x, line.getStart().x);
                }
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

                Vector2f min = box.getLocalMin();
                Vector2f max = box.getLocalMin();

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
        public static boolean lineAndAABB(Line2D line, AABB box) {
                if(pointInAABB(line.getStart(), box) || pointInAABB(line.getEnd(),box)) {
                        return true;
                }

                Vector2f unitVector = new Vector2f(line.getEnd()).sub(line.getStart());
                unitVector.normalize();
                unitVector.x = (unitVector.x != 0) ? 1.0f / unitVector.x : 0f;
                unitVector.y = (unitVector.y != 0) ? 1.0f / unitVector.y : 0f;

                Vector2f min = box.getMin();
                min.sub(line.getStart()).mul(unitVector);
                Vector2f max = box.getMax();
                max.sub(line.getStart()).mul(unitVector);

                float tMin = Math.max(Math.min(min.x, max.x), Math.min(min.y, max.y));
                float tMax = Math.min(Math.max(min.x, max.x), Math.max(min.y, max.y));
                if(tMax < 0 || tMin > tMax){
                        return false;
                }

                float t = (tMin < 0f) ? tMax : tMin;
                return t > 0f && t * t < line.lengthSquared();
        }

        public static boolean lineAndBox2D (Line2D line, Box2D box) {
                float theta = - box.getRigidbody().getRotation();
                Vector2f centre = box.getRigidbody().getPosition();
                Vector2f localStart = new Vector2f(line.getStart());
                Vector2f localEnd = new Vector2f(line.getEnd());
                KappaMath.rotate(localStart, theta, centre);
                KappaMath.rotate(localEnd, theta, centre);

                Line2D localLine = new Line2D(localStart, localEnd);
                AABB aabb = new AABB(box.getLocalMin(), box.getLocalMin());

                return lineAndAABB(localLine, aabb);
        }

        //Raycasts
        public static boolean raycast(Circle circle, Ray2D ray, RaycastResult result) {
                RaycastResult.reset(result);

                Vector2f originToCircle = new Vector2f(circle.getCentre().sub(ray.getOrigin()));
                float radiusSquared = circle.getRadius() * circle.getRadius();
                float originToCircleLengthSquared = originToCircle.lengthSquared();

                //Project the vector from the ray origin onto the direction of the ray
                float a = originToCircle.dot(ray.getDirection());
                float bSq = originToCircleLengthSquared - (a * a);
                if(radiusSquared - bSq < 0.0f){
                        return false;
                }

                float f = (float)Math.sqrt(radiusSquared - bSq);
                float t = 0;
                if(originToCircleLengthSquared < radiusSquared) {
                        //Ray starts inside the circle
                        t = a + f;
                }else{
                        t = a-f;
                }

                if(result != null){
                        Vector2f point = new Vector2f(ray.getOrigin()).add(new Vector2f(ray.getDirection()).mul(t));
                        Vector2f normal = new Vector2f(point).sub(circle.getCentre());
                        normal.normalize();

                        result.init(point, normal, t, true);
                }

                return true;

        }

        public static boolean raycast(AABB box, Ray2D ray, RaycastResult result) {
                RaycastResult.reset(result);
                Vector2f unitVector = ray.getDirection();
                unitVector.normalize();
                unitVector.x = (unitVector.x != 0) ? 1.0f / unitVector.x : 0f;
                unitVector.y = (unitVector.y != 0) ? 1.0f / unitVector.y : 0f;

                Vector2f min = box.getMin();
                min.sub(ray.getOrigin()).mul(unitVector);
                Vector2f max = box.getMax();
                max.sub(ray.getOrigin()).mul(unitVector);

                float tMin = Math.max(Math.min(min.x, max.x), Math.min(min.y, max.y));
                float tMax = Math.min(Math.max(min.x, max.x), Math.max(min.y, max.y));
                if(tMax < 0 || tMin > tMax){
                        return false;
                }

                float t = (tMin < 0f) ? tMax : tMin;
                boolean hit = t > 0.0f;
                // && t * T < ray.getMaximum();
                if(!hit) {
                        return false;
                }
                if(result != null){
                        Vector2f point = new Vector2f(ray.getOrigin()).add(new Vector2f(ray.getDirection()).mul(t));
                        Vector2f normal = new Vector2f(ray.getOrigin()).sub(point);
                        normal.normalize();

                        result.init(point, normal, t, true);
                }
                return true;
        }

        public static boolean raycast(Box2D box, Ray2D ray, RaycastResult result) {
                RaycastResult.reset(result);

                Vector2f size = box.getHalfSize();
                Vector2f xAxis = new Vector2f(1,0);
                Vector2f yAxis = new Vector2f(0,1);
                KappaMath.rotate(xAxis, -box.getRigidbody().getRotation(), new Vector2f(0,0));
                KappaMath.rotate(yAxis, -box.getRigidbody().getRotation(), new Vector2f(0,0));

                Vector2f p = new Vector2f(box.getRigidbody().getPosition().sub(ray.getOrigin()));
                //Project the direction of the ray on each axis of the box
                Vector2f f = new Vector2f(xAxis.dot(ray.getDirection()), yAxis.dot(ray.getDirection()));
                //Next, project p on every axis of the box
                Vector2f e = new Vector2f(xAxis.dot(p), yAxis.dot(p));

                float[] tArray = {0,0,0,0};
                for (int i=0; i < 2; i++){
                        if (KappaMath.compare(f.get(i),0)){
                                //If the ray is parallel to the current axis, and the origin of the
                                //ray is not inside, we have no hit
                                if(-e.get(i) - size.get(i) > 0 || -e.get(i) + size.get(i) < 0) {
                                        return false;
                                }
                                f.setComponent(i, 0.00001f);//set it to small value to avoid divide by 0
                        }
                        tArray[i * 2 + 0] = (e.get(i) + size.get(i)) / f.get(i);//tMax for this axis
                        tArray[i * 2 + 1] = (e.get(i) - size.get(i)) / f.get(i);//tMin for this axis
                }

                float tmin = Math.max(Math.min(tArray[0], tArray[1]), Math.min(tArray[2], tArray[3]));
                float tmax = Math.min(Math.max(tArray[0], tArray[1]), Math.max(tArray[2], tArray[3]));

                float t = (tmin < 0f) ? tmax : tmin;
                boolean hit = t > 0.0f;
                // && t * T < ray.getMaximum();
                if(!hit) {
                        return false;
                }
                if(result != null){
                        Vector2f point = new Vector2f(ray.getOrigin()).add(new Vector2f(ray.getDirection()).mul(t));
                        Vector2f normal = new Vector2f(ray.getOrigin()).sub(point);
                        normal.normalize();

                        result.init(point, normal, t, true);
                }
                return true;
        }

        public static boolean circleAndLine (Circle circle, Line2D line) {
                return lineAndCircle(line, circle);
        }
        public static boolean circleAndCircle(Circle c1, Circle c2) {
                Vector2f vectorBetweenCentres = new Vector2f(c1.getCentre()).sub(c2.getCentre());
                float radiiSum = c1.getRadius() + c2.getRadius();
                return vectorBetweenCentres.lengthSquared() <= radiiSum * radiiSum;
        }
        public static boolean circleAndAABB(Circle circle, AABB box) {
                Vector2f min = box.getMin();
                Vector2f max = box.getMax();

                Vector2f closestPointToCircle = new Vector2f(circle.getCentre());
                if(closestPointToCircle.x < min.x) {
                        closestPointToCircle.x = min.x;
                }else if(closestPointToCircle.x > max.x){
                        closestPointToCircle.x = max.x;
                }

                if(closestPointToCircle.y < min.y) {
                        closestPointToCircle.y = min.y;
                }else if(closestPointToCircle.y > max.y){
                        closestPointToCircle.y = max.y;
                }

                Vector2f circleToBox = new Vector2f(circle.getCentre().sub(closestPointToCircle));
                return circleToBox.lengthSquared() <= circle.getRadius() * circle.getRadius();
        }
        public static boolean circleAndBox2D (Circle circle, Box2D box){
                //Treat box just like an AABB, after we rotate the box
                Vector2f min = new Vector2f();
                Vector2f max = new Vector2f(box.getHalfSize()).mul(2.0f);

                //Create a circle in box's local space
                Vector2f r = new Vector2f(circle.getCentre()).sub(box.getRigidbody().getPosition());
                KappaMath.rotate(r, -box.getRigidbody().getRotation(), new Vector2f(0,0));
                Vector2f localCirclePos = new Vector2f(r).add(box.getHalfSize());

                Vector2f closestPointToCircle = new Vector2f(localCirclePos);
                if(closestPointToCircle.x < min.x) {
                        closestPointToCircle.x = min.x;
                }else if(closestPointToCircle.x > max.x){
                        closestPointToCircle.x = max.x;
                }

                if(closestPointToCircle.y < min.y) {
                        closestPointToCircle.y = min.y;
                }else if(closestPointToCircle.y > max.y){
                        closestPointToCircle.y = max.y;
                }

                Vector2f circleToBox = new Vector2f(localCirclePos.sub(closestPointToCircle));
                return circleToBox.lengthSquared() <= circle.getRadius() * circle.getRadius();
        }

        //AABB vs Primitive tests
        public static boolean AABBandCircle (Circle circle, AABB box) {
                return circleAndAABB(circle, box);
        }
        public static boolean AABBandAABB (AABB box1, AABB box2) {
                //axis aligned
                Vector2f axesToTest[] = {new Vector2f(0,1), new Vector2f(1,0)};
                for (int i=0; i < axesToTest.length; i++) {
                        if (!OverlapOnAxis(box1,box2,axesToTest[i])){
                                return false;
                        }
                }
                return true;
        }

        public static boolean AABBandBox2D (AABB box1, Box2D box2) {
                Vector2f axesToTest[] = {new Vector2f(0,1), new Vector2f(1,0), new Vector2f(0,1), new Vector2f(1,0)};
                KappaMath.rotate(axesToTest[2], box2.getRigidbody().getRotation(), new Vector2f());
                KappaMath.rotate(axesToTest[3], box2.getRigidbody().getRotation(), new Vector2f());
                for (int i=0; i < axesToTest.length; i++) {
                        if (!OverlapOnAxis(box1,box2,axesToTest[i])){
                                return false;
                        }
                }
                return true;
        }

        //Separating Axis theorem helpers
        private static boolean OverlapOnAxis(AABB box1, AABB box2, Vector2f axis){
                Vector2f interval1 = getInterval(box1, axis);
                Vector2f interval2 = getInterval(box2, axis);
                return ((interval2.x <= interval1.y) && (interval1.x <= interval2.y));
        }
        private static boolean OverlapOnAxis(AABB box1, Box2D box2, Vector2f axis){
                Vector2f interval1 = getInterval(box1, axis);
                Vector2f interval2 = getInterval(box2, axis);
                return ((interval2.x <= interval1.y) && (interval1.x <= interval2.y));
        }
        private static boolean OverlapOnAxis(Box2D box1, Box2D box2, Vector2f axis){
                Vector2f interval1 = getInterval(box1, axis);
                Vector2f interval2 = getInterval(box2, axis);
                return ((interval2.x <= interval1.y) && (interval1.x <= interval2.y));
        }
        private static Vector2f getInterval(AABB rectangle, Vector2f axis){
                Vector2f result = new Vector2f(0,0);

                Vector2f min = rectangle.getMin();
                Vector2f max = rectangle.getMax();

                Vector2f vertices[] = {
                        new Vector2f(min.x, min.y), new Vector2f(min.x, max.y), new Vector2f(max.x, min.y), new Vector2f(max.x, max.y)
                };

                result.x = axis.dot(vertices[0]);
                result.y = result.x;
                for(int i=1; i < 4; i++){
                        float projection = axis.dot(vertices[i]);
                        if(projection < result.x){
                                result.x = projection;
                        }
                        if(projection < result.y) {
                                result.y = projection;
                        }
                }

                return result;
        }

        private static Vector2f getInterval(Box2D rectangle, Vector2f axis){
                Vector2f result = new Vector2f(0,0);

                Vector2f vertices[] = rectangle.getVertices();

                result.x = axis.dot(vertices[0]);
                result.y = result.x;
                for(int i=1; i < 4; i++){
                        float projection = axis.dot(vertices[i]);
                        if(projection < result.x){
                                result.x = projection;
                        }
                        if(projection < result.y) {
                                result.y = projection;
                        }
                }

                return result;
        }
}
