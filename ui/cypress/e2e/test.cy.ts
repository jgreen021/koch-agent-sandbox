/// <reference types="cypress" />

describe('Authentication Flow', () => {
  beforeEach(() => {
    cy.visit('/login');
  });

  it('successful login redirects to dashboard', () => {
    // Mock the login API
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        accessToken: 'mock-access-token.eyJzY29wZSI6IlJPTEVfT1BFUkFUT1IiLCJleHAiOjI1MzQwMjMwNDAwMH0.sig',
        refreshToken: 'mock-refresh-token',
        expiresIn: 3600
      }
    }).as('loginRequest');

    cy.get('ion-input[label="Username"] input').type('admin');
    cy.get('ion-input[label="Password"] input').type('password');
    cy.get('ion-button[type="submit"]').click();

    cy.wait('@loginRequest');
    cy.url().should('include', '/dashboard');
    cy.contains('Real-Time Kiln Telemetry');
  });

  it('failed login shows error message', () => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 401,
      body: { message: 'Invalid credentials' }
    }).as('loginRequest');

    cy.get('ion-input[label="Username"] input').type('admin');
    cy.get('ion-input[label="Password"] input').type('wrong-password');
    cy.get('ion-button[type="submit"]').click();

    cy.wait('@loginRequest');
    cy.contains('Invalid credentials');
  });
});