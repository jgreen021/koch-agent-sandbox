import React, { useEffect } from 'react';
import { Redirect, Route } from 'react-router-dom';
import { IonApp, IonRouterOutlet, setupIonicReact } from '@ionic/react';
import { IonReactRouter } from '@ionic/react-router';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import ProtectedRoute from './components/ProtectedRoute';
import ErrorBoundary from './components/ErrorBoundary';
import { useAppStore } from './store/useAppStore';

/* Core CSS required for Ionic components to work properly */
import '@ionic/react/css/core.css';

/* Basic CSS for apps built with Ionic */
import '@ionic/react/css/normalize.css';
import '@ionic/react/css/structure.css';
import '@ionic/react/css/typography.css';

/* Optional CSS utils that can be commented out */
import '@ionic/react/css/padding.css';
import '@ionic/react/css/float-elements.css';
import '@ionic/react/css/text-alignment.css';
import '@ionic/react/css/text-transformation.css';
import '@ionic/react/css/flex-utils.css';
import '@ionic/react/css/display.css';

/**
 * Ionic Dark Mode
 * -----------------------------------------------------
 * For more info, please see:
 * https://ionicframework.com/docs/theming/dark-mode
 */

/* import '@ionic/react/css/palettes/dark.always.css'; */
/* import '@ionic/react/css/palettes/dark.class.css'; */
import '@ionic/react/css/palettes/dark.system.css';

/* Theme variables */
import './theme/variables.css';

setupIonicReact();

const queryClient = new QueryClient();

const App: React.FC = () => {
  const darkMode = useAppStore(state => state.darkMode);

  // Apply dark mode theme globally to the document root
  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add('ion-palette-dark');
      document.body.classList.add('dark');
    } else {
      document.documentElement.classList.remove('ion-palette-dark');
      document.body.classList.remove('dark');
    }
  }, [darkMode]);

  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <IonApp>
          <IonReactRouter>
            <IonRouterOutlet>
              <ProtectedRoute exact path="/dashboard" component={Dashboard} requiredRoles={['OPERATOR', 'ADMIN']} />
              <Route exact path="/login">
                <Login />
              </Route>
              <Route exact path="/home">
                <Redirect to="/login" />
              </Route>
              <Route exact path="/">
                <Redirect to="/login" />
              </Route>
            </IonRouterOutlet>
          </IonReactRouter>
        </IonApp>
      </QueryClientProvider>
    </ErrorBoundary>
  );
};

export default App;
