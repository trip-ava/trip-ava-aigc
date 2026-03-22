# Trip-AVA AIGC - Backend

> AVA AI Digital Human Travel Guide - Backend Service

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.0-green)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.8-red)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Proprietary-red)](LICENSE)

**Trip-AVA AIGC** 是 AVA AI 数字人向导的后端服务，采用 Java + Spring Boot 构建，提供 AI 对话、语音合成等核心功能。

## ✨ 特性

- 🤖 **OpenAI 集成** - 智能对话，支持上下文记忆
- 🎙️ **语音输入** - 语音转文字 (AWS Transcribe)
- 🔊 **语音合成** - 文字转语音 (AWS Polly)
- 🌐 **CORS 支持** - 跨域访问配置
- 🐳 **Docker 支持** - 容器化部署
- 📊 **健康检查** - 服务状态监控

## 🛠️ 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 2.6.0 | Web 框架 |
| Maven | 3.8+ | 构建工具 |
| OkHttp | 4.11.0 | HTTP 客户端 |
| Lombok | latest | 简化 Java 代码 |
| Jackson | 2.13.2 | JSON 处理 |

## 📁 项目结构

```
trip-ava-aigc/
├── src/main/java/group/rxcloud/ava/aigc/
│   ├── SpringMain.java              # 主入口
│   ├── config/
│   │   └── CorsConfig.java          # CORS 配置
│   ├── controller/
│   │   └── AvaController.java       # API 控制器
│   ├── service/
│   │   ├── OpenAiChatService.java   # OpenAI 对话服务
│   │   └── AvaService.java          # AVA 核心服务
│   ├── entity/                      # 数据实体
│   └── utils/                       # 工具类
├── src/main/resources/
│   └── application.properties       # 配置文件
├── Dockerfile                       # Docker 镜像
├── docker-compose.yml                # Docker Compose 配置
├── pom.xml                          # Maven 依赖
└── README.md
```

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.8+
- OpenAI API Key

### 本地运行

```bash
# 1. 克隆项目
git clone https://github.com/ava-agent/trip-ava-aigc.git
cd trip-ava-aigc

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env，添加 OPENAI_API_KEY

# 3. 构建项目
mvn clean package

# 4. 运行服务
mvn spring-boot:run
```

服务启动后访问: http://localhost:8080/

### Docker 运行

```bash
# 构建镜像
docker build -t trip-ava-aigc .

# 运行容器
docker run -p 8080:8080 \
  -e OPENAI_API_KEY=your_key \
  trip-ava-aigc
```

## 📡 API 接口

### 聊天接口

```http
POST /ava/chat
Content-Type: application/json

{
  "userId": "user123",
  "conversationId": "conv456",
  "message": {
    "role": "user",
    "content": "推荐云南的旅行目的地"
  }
}
```

**响应**:
```json
{
  "conversationId": "conv456",
  "message": {
    "role": "assistant",
    "content": "根据您的兴趣，我推荐...",
    "timestamp": 1234567890
  }
}
```

### 语音接口

```http
POST /ava/voice
Content-Type: multipart/form-data

audio: [音频文件]
conversationId: conv456
userId: user123
```

### 其他接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/ava/hello` | POST | 获取问候语 |
| `/ava/hello/voice` | POST | 文字转语音 |
| `/ava/health` | GET | 健康检查 |

## 🔧 配置说明

### 环境变量

```bash
# OpenAI
export OPENAI_API_KEY=sk-...
export OPENAI_MODEL=gpt-3.5-turbo

# AWS (可选)
export AWS_ACCESS_KEY_ID=AKIA...
export AWS_SECRET_ACCESS_KEY=...
export AWS_REGION=ap-southeast-1
```

## 🐳 Docker 部署

### 使用 Docker Compose

```bash
docker-compose up -d
```

### 单独部署

```bash
# 构建镜像
docker build -t trip-ava-aigc .

# 运行容器
docker run -d \
  --name ava-backend \
  -p 8080:8080 \
  -e OPENAI_API_KEY=your_key \
  trip-ava-aigc
```

## 🔗 相关仓库

- **[ava](https://github.com/ava-agent/ava)** - 设计文档 (Private)
- **[trip-ava](https://github.com/ava-agent/trip-ava)** - 前端应用

## 📄 许可证

专有软件 - Copyright © 2024

---

**Trip-AVA AIGC** - 简洁专注的 AI 服务后端
