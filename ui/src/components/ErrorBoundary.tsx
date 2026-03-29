import React, { Component, ErrorInfo, ReactNode } from 'react';
import { IonContent, IonPage, IonHeader, IonToolbar, IonTitle, IonButton, IonIcon } from '@ionic/react';
import { alertCircleOutline, refreshOutline } from 'ionicons/icons';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Uncaught error:', error, errorInfo);
  }

  private handleReload = () => {
    window.location.reload();
  };

  public render() {
    if (this.state.hasError) {
      return (
        <IonPage className="bg-gray-100 dark:bg-gray-900">
          <IonHeader>
            <IonToolbar color="danger">
              <IonTitle>Application Error</IonTitle>
            </IonToolbar>
          </IonHeader>
          <IonContent className="ion-padding">
            <div className="flex flex-col items-center justify-center p-8 h-full space-y-6 text-center">
              <IonIcon icon={alertCircleOutline} style={{ fontSize: '64px', color: 'var(--ion-color-danger)' }} />
              <h1 className="text-2xl font-bold dark:text-white">Something went wrong</h1>
              <p className="text-gray-600 dark:text-gray-400 max-w-md">
                The application encountered an unexpected error. This has been logged, and you can try to reload the page to continue.
              </p>
              <IonButton onClick={this.handleReload} expand="block" shape="round">
                <IonIcon slot="start" icon={refreshOutline} />
                Reload Application
              </IonButton>
              {process.env.NODE_ENV === 'development' && (
                <div className="mt-8 p-4 bg-red-50 dark:bg-red-900/20 rounded-lg text-left overflow-auto max-w-2xl max-h-48 border border-red-200 dark:border-red-800">
                  <pre className="text-xs text-red-700 dark:text-red-400">
                    {this.state.error?.stack}
                  </pre>
                </div>
              )}
            </div>
          </IonContent>
        </IonPage>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
