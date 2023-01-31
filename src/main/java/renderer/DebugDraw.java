package renderer;

import Kappa.Window;
import org.joml.Vector2f;
import org.joml.Vector3f;
import util.AssetPool;
import util.JMath;

import java.lang.Math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class DebugDraw {
    private static int MAX_LINES = 500;

    private static List<Line2D> lines = new ArrayList<>();
    //6 floats per vertex, 2 vertices per line
    private static float[] vertexArray = new float[MAX_LINES *6 * 2];
    private static Shader shader = AssetPool.getShader("assets/shaders/debugLine2D.glsl");

    private static int vaoID;
    private static int vboID;

    private static boolean started = false;

    public static void start(){
        //Generate vao
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        //Create the vbo and buffer some memory
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexArray.length * Float.BYTES, GL_DYNAMIC_DRAW);

        //Enable the vertex array attributes
        glVertexAttribPointer(0,3,GL_FLOAT,false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1,3,GL_FLOAT,false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glLineWidth(4.0f);
    }

    public static void  beginFrame(){
        if(!started){
            start();
            started = true;
        }

        //Remove dead lines
        for (int i = 0; i < lines.size(); i++){
            if (lines.get(i).beginFrame() < 0){
                lines.remove(i);
                i--;
            }
        }
    }
    public static void draw(){
        if (lines.size() <= 0) return;

        int index = 0;
        for (Line2D line : lines){
            for (int i=0; i < 2; i++){
                Vector2f position = i == 0 ?line.getFrom() : line.getTo();
                Vector3f colour = line.getColour();

                //Load position
                vertexArray[index] = position.x;
                vertexArray[index + 1] = position.y;
                vertexArray[index + 2] = -10.0f;

                //Load colour
                vertexArray[index + 3] = colour.x;
                vertexArray[index + 4] = colour.y;
                vertexArray[index + 5] = colour.z;
                index += 6;
            }
        }

        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, Arrays.copyOfRange(vertexArray, 0, lines.size() * 6 * 2));

        //Use shader
        shader.use();
        shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());

        //Bind the vao
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        //Draw the batch
        glDrawArrays(GL_LINES, 0, lines.size() * 6 * 2);
        //Bresenham's line algorithm

        //Disable location
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        //Unbind shader
        shader.detach();
    }

    //======================================
    //Add line2D methods
    //======================================
    public static void addLine2D(Vector2f from, Vector2f to){
        //Add constants for common colours
        addLine2D(from, to, new Vector3f(0,1,0), 1);
    }
    public static void addLine2D(Vector2f from, Vector2f to,Vector3f colour){
        //Add constants for common colours
        addLine2D(from, to, colour, 1);
    }

    public static void addLine2D(Vector2f from, Vector2f to, Vector3f colour, int lifetime){
        if (lines.size() >= MAX_LINES) return;
        DebugDraw.lines.add(new Line2D(from, to, colour, lifetime));
    }

    //======================================
    //Add Box2D methods
    //======================================
    public static void addBox2D(Vector2f centre, Vector2f dimensions,float rotation){
        //Add constants for common colours
        addBox2D(centre, dimensions, rotation, new Vector3f(0,1,0), 1);
    }
    public static void addBox2D(Vector2f centre, Vector2f dimensions,float rotation, Vector3f colour){
        //Add constants for common colours
        addBox2D(centre, dimensions, rotation, colour, 1);
    }
    public static void addBox2D(Vector2f centre, Vector2f dimensions,float rotation, Vector3f colour, int lifetime){
        Vector2f min = new Vector2f(centre).sub(new Vector2f(dimensions).mul(0.5f));
        Vector2f max = new Vector2f(centre).add(new Vector2f(dimensions).mul(0.5f));

        Vector2f[] vertices = {
            new Vector2f(min.x, min.y), new Vector2f(min.x, max.y),
            new Vector2f(max.x, max.y), new Vector2f(max.x, min.y)
         };

        if (rotation != 0.0f) {
            for (Vector2f vert : vertices){
                JMath.rotate(vert, rotation, centre);
            }
        }

        addLine2D(vertices[0], vertices[1], colour, lifetime);
        addLine2D(vertices[1], vertices[2], colour, lifetime);
        addLine2D(vertices[2], vertices[3], colour, lifetime);
        addLine2D(vertices[0], vertices[3], colour, lifetime);
    }

    //======================================
    //Add Circle methods
    //======================================
    public static void addCircle(Vector2f centre, float radius){
        //Add constants for common colours
        addCircle(centre, radius, new Vector3f(0,1,0), 1);
    }
    public static void addCircle(Vector2f centre, float radius, Vector3f colour){
        //Add constants for common colours
        addCircle(centre, radius, colour, 1);
    }

    public static void addCircle(Vector2f centre, float radius, Vector3f colour, int lifetime){
        Vector2f[] points = new Vector2f[36];
        int increment = 360 / points.length;
        int currentAngle = 0;
        for (int i=0; i < points.length; i++){
            Vector2f tmp = new Vector2f(0, radius);
            JMath.rotate(tmp, currentAngle, new Vector2f());
            points[i] = new Vector2f(tmp).add((centre));

            if(i > 0){
                addLine2D(points[i-1], points[i], colour, lifetime);
            }
            currentAngle += increment;
        }
        addLine2D(points[points.length - 1], points[0], colour, lifetime);
    }
}
