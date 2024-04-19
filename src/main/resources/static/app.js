function resetForm() {
    document.getElementById('name').value = '';
    document.getElementById('surname').value = '';
    document.getElementById('personID').value = '';
    document.getElementById('userId').value = '';
    document.getElementById('userDetails').checked = false;
    document.getElementById('allUsersDetails').checked = false;
    document.getElementById('updateId').value = '';
    document.getElementById('updateName').value = '';
    document.getElementById('updateSurname').value = '';
    document.getElementById('deleteId').value = '';
}


function createUser() {
    const name = document.getElementById('name').value.trim();
    const surname = document.getElementById('surname').value.trim();
    const personID = document.getElementById('personID').value.trim();

    const user = {
        name: name,
        surname: surname,
        personID: personID
    };

    fetch('/api/v1/user', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(user)
    })
    .then(response => {
        console.log(response);
        if (!response.ok) {
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.indexOf('application/json') !== -1) {
                return response.json().then(data => {
                    console.log("Error data:", data);
                    throw new Error(data.message || 'Unknown error');
                });
            } else {
                return response.text().then(text => {
                    throw new Error(text || 'Unknown error');
                });
            }
        }
        return response.json();
    })
    .then(data => {
        document.getElementById('createUserOutput').innerText = 'User created successfully: ' + JSON.stringify(data, null, 2);
    })
    .catch(error => {
        console.log("Caught error:", error);
        document.getElementById('createUserOutput').innerText = 'Error: ' + error.message;
    });
}


function getUser() {
    const userId = document.getElementById('userId').value;
    const withDetails = document.getElementById('userDetails').checked;

    const url = `/api/v1/user/${userId}?detail=${withDetails}`;

    fetch(url)
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP status ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        document.getElementById('userData').innerText = JSON.stringify(data, null, 2);
    })
    .catch(error => {
        document.getElementById('userData').innerText = 'Error: ' + error.message;
    });
}


function getAllUsers() {
    const withDetails = document.getElementById('allUsersDetails').checked;

    const url = `/api/v1/users?detail=${withDetails}`;

    fetch(url)
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP status ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        document.getElementById('allUsersData').innerText = JSON.stringify(data, null, 2);
    })
    .catch(error => {
        document.getElementById('allUsersData').innerText = 'Error: ' + error.message;
    });
}



function updateUser() {
    const userId = document.getElementById('updateId').value;
    const name = document.getElementById('updateName').value;
    const surname = document.getElementById('updateSurname').value;

    if (!userId) {
        document.getElementById('updateUserOutput').innerText = 'Error: User ID must be provided.';
        return;
    }
    if (!name.trim()) {
        document.getElementById('updateUserOutput').innerText = 'Error: Name is required and cannot be empty.';
        return;
    }

    const user = {
        id: Number(userId),
        name: name,
        surname: surname
    };

    fetch('/api/v1/user', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(user)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP status ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        document.getElementById('updateUserOutput').innerText = JSON.stringify(data, null, 2);
    })
    .catch(error => {
        document.getElementById('updateUserOutput').innerText = 'Error: ' + error.message;
    });
}




function deleteUser() {
    const userId = document.getElementById('deleteId').value;

    if (!userId) {
        document.getElementById('deleteUserOutput').innerText = 'Error: User ID must be provided.';
        return;
    }

    fetch(`/api/v1/user/${userId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP status ${response.status}`);
        }
        return response.text();
    })
    .then(message => {
        document.getElementById('deleteUserOutput').innerText = message;
    })
    .catch(error => {
        document.getElementById('deleteUserOutput').innerText = 'Error: ' + error.message;
    });
}
