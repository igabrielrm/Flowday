import path from 'path';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  base: '/app/',
  resolve: {
    dedupe: ['react', 'react-dom', '@emotion/react', '@emotion/styled'],
    alias: {
      react: path.resolve(__dirname, 'node_modules/react'),
      'react-dom': path.resolve(__dirname, 'node_modules/react-dom'),
    },
  },
  define: {
    // sockjs-client espera `global` (Node). En el navegador no existe.
    global: 'globalThis',
  },
  build: {
    outDir: '../src/main/resources/static/app',
    emptyOutDir: true,
  },
  server: {
    port: 5173,
    hmr: {
      path: '/app/',
    },
    proxy: {
      '/api': 'http://localhost:8080',
      '/manifest.json': 'http://localhost:8080',
      '/sw.js': 'http://localhost:8080',
      '/images': 'http://localhost:8080',
      '/ws': {
        target: 'http://localhost:8080',
        ws: true,
        changeOrigin: true,
      },
      '/uploads': 'http://localhost:8080',
      '/oauth2': 'http://localhost:8080',
      '/login/oauth2': 'http://localhost:8080',
      '/internal': 'http://localhost:8080',
      '/admin': 'http://localhost:8080',
    },
  },
  optimizeDeps: {
    include: ['react', 'react-dom', '@emotion/react', '@emotion/styled', '@mui/material', 'sockjs-client'],
  },
});
