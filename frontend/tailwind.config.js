/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        display: ['Poppins', 'Inter', 'sans-serif'],
      },
      colors: {
        flow: {
          50: '#f0f6ff',
          100: '#dce8fc',
          200: '#b8d0f8',
          300: '#89b0f0',
          400: '#5082ef',
          500: '#3b6fd4',
          600: '#2d57ad',
          700: '#254689',
          800: '#223b6f',
          900: '#20335c',
        },
        sage: {
          50: '#f4f9f4',
          100: '#e3f0e5',
          200: '#c8e2cd',
          300: '#9dccaa',
          400: '#6faf82',
          500: '#4d9264',
        },
        blush: {
          50: '#fdf2f6',
          100: '#fce7ef',
          200: '#fbcfe0',
          300: '#f9a8c4',
        },
      },
      boxShadow: {
        card: '0 4px 24px -4px rgba(15, 23, 42, 0.1)',
        elevated: '0 16px 48px -12px rgba(15, 23, 42, 0.18)',
      },
      backgroundImage: {
        'pattern-light': "url('/images/bg-pattern-light.png')",
        'pattern-dark': "url('/images/bg-pattern-dark.png')",
      },
    },
  },
  plugins: [],
};
