package org.example;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    // Referência da janela
    private long window;

    // Câmera (posição e rotação)
    float camX = 0.0f;
    float camY = 5.0f;
    float camZ = 20.0f;

    float camRotY = 0.0f; // (rotação lateral - ainda não usamos)
    // Rotação da câmera
    float camRotX = 0.0f;  // Rotação vertical (olhar pra cima/baixo)
    float speed = 0.5f;


    public void run() {
        System.out.println("Iniciando Mini Minecraft Pre-Classic...");
        init();
        loop();

        // Libera callbacks e fecha a janela
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Termina GLFW
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Configura callback de erro do GLFW
        GLFWErrorCallback.createPrint(System.err).set();

        // Inicializa GLFW
        if (!glfwInit())
            throw new IllegalStateException("Não foi possível inicializar GLFW");

        // Configurações da janela GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Cria a janela
        window = glfwCreateWindow(800, 600, "Mini Minecraft", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Falha ao criar janela GLFW");

        // Centraliza a janela na tela
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        // Torna o contexto OpenGL atual
        glfwMakeContextCurrent(window);

        // Ativa V-Sync
        glfwSwapInterval(1);

        // Configura captura do cursor dentro da janela (mouse invisível e preso)
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        // Centraliza o cursor inicialmente
        glfwSetCursorPos(window, 400, 300);

        // Callback do movimento do mouse
        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            final float sensitivity = 0.1f;

            float dx = (float)(xpos - 400);
            float dy = (float)(300 - ypos);

            camRotY += dx * sensitivity;
            camRotX += dy * sensitivity;

            // Limita a rotação vertical para não virar de ponta cabeça
            if (camRotX > 89.0f) camRotX = 89.0f;
            if (camRotX < -89.0f) camRotX = -89.0f;

            // Reset cursor para centro da janela para capturar movimento relativo
            glfwSetCursorPos(window, 400, 300);
        });

        // Mostra a janela depois das configurações
        glfwShowWindow(window);
    }

    private void loop() {
        // Cria as capacidades do OpenGL
        GL.createCapabilities();

        // Define a cor de fundo (céu azul)
        glClearColor(0.5f, 0.8f, 1.0f, 0.0f);

        // Habilita profundidade
        glEnable(GL_DEPTH_TEST);

        while (!glfwWindowShouldClose(window)) {
            processInput();

            // Limpa a tela e o buffer de profundidade
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            float aspect = 800f / 600f;
            gluPerspective(70.0f, aspect, 0.1f, 100.0f);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            glRotatef(camRotX, 1f, 0f, 0f); // Rotação vertical
            glRotatef(camRotY, 0f, 1f, 0f); // Rotação horizontal
            glTranslatef(-camX, -camY, -camZ); // Translada posição

// Loop para desenhar um terreno plano
            int renderDistance = 25; // raio do terreno visível em blocos

            int camBlockX = (int) Math.floor(camX / 2.0f);
            int camBlockZ = (int) Math.floor(camZ / 2.0f);

            for (int x = camBlockX - renderDistance; x <= camBlockX + renderDistance; x++) {
                for (int z = camBlockZ - renderDistance; z <= camBlockZ + renderDistance; z++) {
                    glPushMatrix();
                    glTranslatef(x * 2.0f, 0.0f, z * 2.0f);
                    drawCube();
                    glPopMatrix();
                }
            }



            // --- Aqui você pode desenhar cubos futuramente ---



            // Troca os buffers
            glfwSwapBuffers(window);

            // Processa eventos de input
            glfwPollEvents();
        }
    }

    private void drawCube() {
        glBegin(GL_QUADS);
        glColor3f(0.2f, 0.8f, 0.3f); // Verde grama

        // Topo
        glVertex3f(-1, 1, -1);
        glVertex3f(1, 1, -1);
        glVertex3f(1, 1, 1);
        glVertex3f(-1, 1, 1);

        // Base
        glVertex3f(-1, -1, -1);
        glVertex3f(1, -1, -1);
        glVertex3f(1, -1, 1);
        glVertex3f(-1, -1, 1);

        // Frente
        glVertex3f(-1, -1, 1);
        glVertex3f(1, -1, 1);
        glVertex3f(1, 1, 1);
        glVertex3f(-1, 1, 1);

        // Trás
        glVertex3f(-1, -1, -1);
        glVertex3f(1, -1, -1);
        glVertex3f(1, 1, -1);
        glVertex3f(-1, 1, -1);

        // Esquerda
        glVertex3f(-1, -1, -1);
        glVertex3f(-1, -1, 1);
        glVertex3f(-1, 1, 1);
        glVertex3f(-1, 1, -1);

        // Direita
        glVertex3f(1, -1, -1);
        glVertex3f(1, -1, 1);
        glVertex3f(1, 1, 1);
        glVertex3f(1, 1, -1);
        glEnd();
    }


    private void gluPerspective(float fovY, float aspect, float zNear, float zFar) {
        float fH = (float) Math.tan(Math.toRadians(fovY / 2)) * zNear;
        float fW = fH * aspect;
        glFrustum(-fW, fW, -fH, fH, zNear, zFar);
    }

    private void processInput() {
        float speed = 0.2f;

        float forwardX = (float) Math.sin(Math.toRadians(camRotY));
        float forwardZ = (float) -Math.cos(Math.toRadians(camRotY));

        float rightX = (float) Math.sin(Math.toRadians(camRotY + 90));
        float rightZ = (float) -Math.cos(Math.toRadians(camRotY + 90));

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            camX += forwardX * speed;
            camZ += forwardZ * speed;
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            camX -= forwardX * speed;
            camZ -= forwardZ * speed;
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            camX -= rightX * speed;
            camZ -= rightZ * speed;
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            camX += rightX * speed;
            camZ += rightZ * speed;
        }

        // CamY fixo para andar no chão
        camY = 2.0f;
    }


    public static void main(String[] args) {
        new Main().run();
    }
    }