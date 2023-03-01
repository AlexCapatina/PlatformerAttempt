package physics2d.rigidbody;

import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class CollisionManifold {
    private boolean isColliding;
    private Vector2f normal;
    private List<Vector2f> contactPoint;
    private float depth;

    public CollisionManifold(){
        normal = new Vector2f();
        depth = 0.0f;
    }

    public CollisionManifold(Vector2f normal, float depth) {
        this.normal = normal;
        this.contactPoint = new ArrayList<>();
        this.depth = depth;
    }

    public void addContactPoint (Vector2f contact) {
        this.contactPoint.add(contact);
    }

    public Vector2f getNormal() {
        return normal;
    }

    public List<Vector2f> getContactPoint() {
        return contactPoint;
    }

    public float getDepth() {
        return depth;
    }

    public boolean isColliding() {
        return this.isColliding;
    }
}
