package com.aut.shoomal;

public class App
{
    public static void main(String[] args)
    {
        Server server = new Server(8080);
        server.run();
    }
}