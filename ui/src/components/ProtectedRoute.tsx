import React from 'react';
import { Route, Redirect, RouteProps } from 'react-router-dom';
import { useAppStore } from '../store/useAppStore';

interface ProtectedRouteProps extends RouteProps {
    component: React.ComponentType<any>;
    requiredRoles?: string[];
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ component: Component, requiredRoles, ...rest }) => {
    const token = useAppStore(state => state.token);
    const userRole = useAppStore(state => state.userRole);
    const isTokenExpired = useAppStore(state => state.isTokenExpired);

    return (
        <Route
            {...rest}
            render={(props) => {
                if (!token || isTokenExpired()) {
                    // Not logged in or session expired
                    return <Redirect to="/login" />;
                }

                if (requiredRoles && userRole && !requiredRoles.includes(userRole)) {
                    // Role not authorized
                    return <Redirect to="/login" />;
                }

                return <Component {...props} />;
            }}
        />
    );
};

export default ProtectedRoute;
