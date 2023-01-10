# Java Tetris Game

## Introduction

这是一个基于 Java Swing 和 Socket 实现的俄罗斯方块游戏联机对战游戏，支持多人同时在线对战。

## Requirements

- JDK 1.15+
- MySQL 8.0+

## Run

1. 使用 `db.sql` 文件创建表。 
2. 修改 `src/org/tetris/Database.java` 文件中的数据库配置。
3. 修改 `src/org/tetris/Server.java` 文件中的服务器端口设置。
4. 运行 `src/org/tetris/Server.java` 文件启动服务器。
5. 运行 `src/org/tetris/Client.java` 文件启动客户端。