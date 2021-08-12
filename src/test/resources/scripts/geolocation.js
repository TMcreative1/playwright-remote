window.geolocationPromise = new Promise(resolve => {
    navigator.geolocation.getCurrentPosition(position => {
        resolve({latitude: position.coords.latitude, longitude: position.coords.longitude});
    });
});