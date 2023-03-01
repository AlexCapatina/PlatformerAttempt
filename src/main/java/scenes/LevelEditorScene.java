package scenes;

import Kappa.Camera;
import Kappa.GameObject;
import Kappa.Prefabs;
import Kappa.Transform;
import components.*;
import imgui.ImGui;
import imgui.ImVec2;
import org.joml.Vector2f;
import org.joml.Vector3f;
import physics2d.PhysicsSystem2D;
import physics2d.primitives.Circle;
import physics2d.rigidbody.Rigidbody2D;
import renderer.DebugDraw;
import util.AssetPool;

public class LevelEditorScene extends Scene {

    //private GameObject obj1;
    private Spritesheet sprites;
    GameObject levelEditor = this.createGameObject("Level editor");
    PhysicsSystem2D physics = new PhysicsSystem2D(1.0f / 60.0f, new Vector2f(0, -10));
    Transform obj1, obj2;
    Rigidbody2D rb1, rb2;



    public LevelEditorScene() {

    }

    @Override
    public void init() {
        loadResources();

        obj1 = new Transform(new Vector2f(100, 500));
        obj2 = new Transform(new Vector2f(100, 300));

        rb1 = new Rigidbody2D();
        rb2 = new Rigidbody2D();
        rb1.setRawTransform(obj1);
        rb2.setRawTransform(obj2);
        rb1.setMass(100.0f);
        rb2.setMass(200.0f);

        Circle c1 = new Circle();
        c1.setRadius(10.0f);
        c1.setRigidbody(rb1);
        Circle c2 = new Circle();
        c1.setRadius(20.0f);
        c1.setRigidbody(rb2);
        rb1.setCollider(c1);
        rb2.setCollider(c2);

        physics.addRigidbody(rb1, true);
        physics.addRigidbody(rb2, false);

        sprites = AssetPool.getSpritesheet("assets/images/spritesheets/decorationsAndBlocks.png");
        Spritesheet gizmos = AssetPool.getSpritesheet("assets/images/gizmos.png");

        this.camera = new Camera(new Vector2f(-250, 0));
        levelEditor.addComponent(new MouseControls());
        //levelEditor.addComponent(new GridLines());
        levelEditor.addComponent(new EditorCamera(this.camera));
        levelEditor.addComponent(new GizmoSystem(gizmos));

        levelEditor.start();

        //DebugDraw.addLine2D(new Vector2f(0,0), new Vector2f(800, 800), new Vector3f(1,0,0), 120);


    }

    private void loadResources() {
        AssetPool.getShader("assets/shaders/default.glsl");

        AssetPool.addSpriteSheet("assets/images/spritesheets/decorationsAndBlocks.png",
                new Spritesheet(AssetPool.getTexture("assets/images/spritesheets/decorationsAndBlocks.png"),
                        16, 16, 81, 0));
        AssetPool.addSpriteSheet("assets/images/gizmos.png", new Spritesheet(AssetPool.getTexture("assets/images/gizmos.png"),
                24, 48, 3, 0));
        AssetPool.getTexture("assets/images/blendImage2.png");

        for(GameObject g : gameObjects){
            if(g.getComponent(SpriteRenderer.class) != null){
                SpriteRenderer spr = g.getComponent(SpriteRenderer.class);
                if(spr.getTexture() != null){
                    spr.setTexture((AssetPool.getTexture(spr.getTexture().getFilepath())));
                }
            }
        }
    }

    float x = 0.0f;
    float y = 0.0f;
    float angle = 0.0f;
    @Override
    public void update(float dt) {
        levelEditor.update(dt);
        this.camera.adjustProjection();
        //DebugDraw.addCircle(new Vector2f(x,y), 64, new Vector3f(0,1,0), 1);
        DebugDraw.addCircle(obj1.position, 10.0f, new Vector3f(1,0,0));
        DebugDraw.addCircle(obj2.position, 20.0f, new Vector3f(0,1,0));
        physics.update(dt);
        x += 50.0f * dt;
        y += 50.0f * dt;
        //DebugDraw.addBox2D(new Vector2f(400,200), new Vector2f(64, 32),angle, new Vector3f(1,0,0), 1);
        angle += 60.0f * dt;
        //mouseControls.update(dt)

//        float x = ((float) Math.sin(t) * 200.0f) + 600;
//        float y = ((float) Math.cos(t) * 200.0f) + 400;
//        t += 0.05f;
//        DebugDraw.addLine2D(new Vector2f(600,400), new Vector2f(x, y), new Vector3f(1,0,0), 120);

        for (GameObject go : this.gameObjects) {
            go.update(dt);
        }
    }

    @Override
    public void render() {
        this.renderer.render();
    }

    @Override
    public void imgui() {
        ImGui.begin("Level editor stuff");
        levelEditor.imgui();
        ImGui.end();

        ImGui.begin("Test window");

        ImVec2 windowPos = new ImVec2();
        ImGui.getWindowPos(windowPos);
        ImVec2 windowSize = new ImVec2();
        ImGui.getWindowSize(windowSize);
        ImVec2 itemSpacing = new ImVec2();
        ImGui.getStyle().getItemSpacing(itemSpacing);

        float windowX2 = windowPos.x + windowSize.x;
        for (int i = 0; i < sprites.size(); i++){
            Sprite sprite = sprites.getSprite(i);
            float spriteWidth = sprite.getWidth() * 4;
            float spriteHeight = sprite.getHeight() * 4;
            int id = sprite.getTexId();
            Vector2f[] texCoords = sprite.getTexCoords();

            ImGui.pushID(i);
            if(ImGui.imageButton(id, spriteWidth, spriteHeight, texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)){
                GameObject object = Prefabs.generateSpriteObject(sprite, 32, 32);
                //Attach to mouse cursor
                //mouseControls.pickUpObject(object);
                levelEditor.getComponent(MouseControls.class).pickUpObject(object);
            }
            ImGui.popID();

            ImVec2 lastButtonPos = new ImVec2();
            ImGui.getItemRectMax(lastButtonPos);
            float lastButtonX2 = lastButtonPos.x;
            float nextButtonX2 = lastButtonX2 + itemSpacing.x + spriteWidth;
            if (i + 1 < sprites.size() && nextButtonX2 < windowX2){
                ImGui.sameLine();
            }
        }

        ImGui.end();
    }
}