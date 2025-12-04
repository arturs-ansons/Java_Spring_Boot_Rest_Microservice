import express from "express";
import path from "path";
import { createProxyMiddleware } from "http-proxy-middleware";
import { fileURLToPath } from "url";

// For ES modules, we need to get __dirname equivalent
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();

// Basic CORS headers manually
app.use((req, res, next) => {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
  res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
  if (req.method === "OPTIONS") {
    return res.sendStatus(200);
  }
  next();
});

// API Proxy configuration
app.use("/api/auth", createProxyMiddleware({
    target: "http://auth-service:8081",
    changeOrigin: true,
    pathRewrite: {
      '^/api/auth': '/api/auth'
    }
}));

app.use("/api/account", createProxyMiddleware({
    target: "http://account-service:8082",
    changeOrigin: true,
    pathRewrite: {
      '^/api/account': '/api/account'
    }
}));

app.use("/api/client", createProxyMiddleware({
    target: "http://client-service:8083",
    changeOrigin: true,
    pathRewrite: {
      '^/api/client': '/api/client'
    }
}));

// Serve frontend static files
const distPath = path.join(__dirname, "dist");
app.use(express.static(distPath));

// Fallback for SPA routes
app.use((req, res) => {
  res.sendFile(path.join(distPath, "index.html"));
});

// Start server
app.listen(5000, () => console.log("Gateway running on port 5000"));