// Caso de prueba 001: el docente solo ve y carga calificaciones de sus materias.
// Requiere el backend en http://localhost:8080 con las migraciones aplicadas (incluida V17).

describe('CP-001: el docente solo ve y carga calificaciones de sus materias', () => {
  it('ejecuta los cinco pasos del caso de prueba', () => {
    // Paso 1: el docente inicia sesión y ve su panel
    cy.visit('/');
    cy.get('form input[type="text"]').type('profesor2');
    cy.get('form input[type="password"]').type('password');
    cy.get('form button[type="submit"]').click();
    cy.contains('a', 'Calificaciones').should('be.visible');

    // Paso 2: la sección lista solo las materias que tiene asignadas
    cy.contains('a', 'Calificaciones').click();
    cy.get('[role="combobox"]').first().click();
    cy.get('[role="option"]')
      .should('contain', 'Historia')
      .and('contain', 'Geografía')
      .and('not.contain', 'Inglés');
    cy.get('[role="option"]').contains('Historia').click();

    // Paso 3: se abre una evaluación, se carga una nota y se guarda
    cy.contains('td', 'Trabajo práctico 1').click();
    cy.get('table').last().find('tbody tr').first().find('input').clear().type('7');
    cy.contains('button', 'Guardar notas').click();
    cy.contains('Notas guardadas').should('be.visible');

    // Paso 4: se recarga la página y la misma planilla muestra la nota guardada
    cy.reload();
    cy.contains('a', 'Calificaciones').click();
    cy.get('[role="combobox"]').first().click();
    cy.get('[role="option"]').contains('Historia').click();
    cy.contains('td', 'Trabajo práctico 1').click();
    cy.get('table').last().find('tbody tr').first().find('input').should('have.value', '7');

    // Paso 5: un alumno de otro curso no aparece en la planilla
    cy.get('table').should('not.contain', 'ALU003');
  });
});
