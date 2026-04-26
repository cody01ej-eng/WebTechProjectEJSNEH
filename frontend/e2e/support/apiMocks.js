export function successEnvelope(data, message = 'OK') {
  return {
    flag: true,
    code: 'SUCCESS',
    message,
    data
  }
}

export function errorEnvelope(message, code = 'UNAUTHORIZED') {
  return {
    flag: false,
    code,
    message
  }
}

export async function mockCsrf(page) {
  await page.route('**/api/auth/csrf', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(successEnvelope({
        token: 'test-csrf-token',
        headerName: 'X-CSRF-TOKEN'
      }, 'CSRF token loaded'))
    })
  })
}

export async function mockSession(page, initialUser = null) {
  let currentUser = initialUser

  await page.route('**/api/auth/me', async (route) => {
    if (!currentUser) {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify(errorEnvelope('Authentication required'))
      })
      return
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(successEnvelope(currentUser, 'Current session loaded'))
    })
  })

  await page.route('**/api/auth/logout', async (route) => {
    currentUser = null
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(successEnvelope({ loggedOut: true }, 'Logout completed'))
    })
  })

  return {
    setCurrentUser(user) {
      currentUser = user
    }
  }
}

export async function mockLogin(page, usersByEmail, sessionController) {
  await page.route('**/api/auth/login', async (route) => {
    const credentials = route.request().postDataJSON()
    const user = usersByEmail[credentials.email]

    if (!user) {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify(errorEnvelope('Authentication required'))
      })
      return
    }

    sessionController.setCurrentUser(user)
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(successEnvelope(user, 'Login completed'))
    })
  })
}
