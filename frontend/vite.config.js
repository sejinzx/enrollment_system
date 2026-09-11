import { defineConfig } from 'vite';
export default defineConfig({server:{port:3000,strictPort:true,proxy:{'/api':{target:process.env.API_TARGET || 'http://localhost:8081',changeOrigin:true}}}});
