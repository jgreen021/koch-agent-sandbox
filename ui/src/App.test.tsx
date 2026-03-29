import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';

test('renders and redirects to login by default', async () => {
    const { baseElement } = render(<App />);
    expect(baseElement).toBeDefined();
    
    // Check if "Operator Sign In" is present (from Login page)
    const title = await screen.findByText(/Operator Sign In/i);
    expect(title).toBeInTheDocument();
});
