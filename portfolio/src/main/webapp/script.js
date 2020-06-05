// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


function getData() {
  document.getElementById('fetch').innerHTML = "";
  var num = document.getElementById("num");
  num = num.options[num.selectedIndex].value;
  var url = "/data?limit=" + num;

  fetch(url).then(response => response.json()).then((messages) => {
    const taskListElement = document.getElementById('fetch');
    messages.forEach((task) => {
      taskListElement.appendChild(createListElement(task));
    })
  });
}

function createListElement(text) {
  const liElement = document.createElement('h1');
  liElement.innerText = text;
  return liElement;
}

/** Tells the server to delete the task. */
function deleteData() {
  const request = new Request('/delete-data', {method: 'POST'});
  fetch(request);

  getData();
}

function logout(){
  const request = new Request('/delete-data', {method: 'POST'});
  fetch(request);

}

var map;
function initMap() {
    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: -34.397, lng: 150.644},
        zoom: 8
    });
}

function fetchBlobstoreUrlAndShowForm() {
  fetch('/blobstore-upload-url')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        const messageForm = document.getElementById('my-form');
        messageForm.action = imageUploadUrl;
        messageForm.classList.remove('hidden');
      });
}
