import { defineConfig, type ViteDevServer } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";
import { componentTagger } from "lovable-tagger";
import * as fs from "fs";

export default defineConfig(({ mode }) => ({
  server: {
    host: "::",
    port: 8080,
    hmr: {
      overlay: false,
    },
  },
  plugins: [
    {
      name: "static-html",
      enforce: "pre" as const,
      configureServer(server: ViteDevServer) {
        server.middlewares.use((req, res, next) => {
          try {
            const url = (req.url || "").split("?")[0];
            if (url.endsWith(".html") && url !== "/index.html") {
              const file = path.join(process.cwd(), "public", url);
              console.log("[static-html] Checking:", file, "exists:", fs.existsSync(file));
              if (fs.existsSync(file)) {
                console.log("[static-html] Serving:", url);
                res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
                res.end(fs.readFileSync(file));
                return;
              }
            }
          } catch (e) {
            console.error("[static-html] Error:", e);
          }
          next();
        });
      },
    },
    react(),
    mode === "development" && componentTagger(),
  ].filter(Boolean),
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
}));