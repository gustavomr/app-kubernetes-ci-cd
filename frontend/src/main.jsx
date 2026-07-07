import React from 'react';
import ReactDOM from 'react-dom/client';
import { RocketFlagProvider } from '@rocketflag/react-sdk';
import App from './App.jsx';
import './App.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <RocketFlagProvider>
      <App />
    </RocketFlagProvider>
  </React.StrictMode>
);
