export const referenceDocs = [
  {
    title: 'Vision and Scope',
    path: '/requirements/vision_scope.md',
    focus: 'Business problem, objectives, scope boundaries, and risks.'
  },
  {
    title: 'Use Cases',
    path: '/requirements/use_cases.md',
    focus: 'Actor workflows from admin setup through WAR and peer evaluation reporting.'
  },
  {
    title: 'Project Glossary',
    path: '/requirements/project_glossary.md',
    focus: 'Domain language for sections, teams, active weeks, WARs, and peer evaluations.'
  }
]

export const implementationRules = [
  'Identify the target use case before writing code.',
  'Keep business rules in services and domain models, not controllers.',
  'Do not introduce features that are not supported by requirements unless they are clearly labeled as enhancements.',
  'Keep requirement traceability visible as each branch grows.'
]
