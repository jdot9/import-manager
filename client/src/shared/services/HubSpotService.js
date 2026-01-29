// Test connection to HubSpot private app
export async function testConnection(event, formData, saveBtnStyleRef, setIsValidToken)
{
  event.preventDefault();
  const token = {accessToken: formData.hubspotAccessToken};
  try {
    const response = await fetch("http://localhost:8080/hubspot-connection-test", {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(token)
    });
    const result = await response.json();
    if (result)
    {
      alert("Connection Successfully made.") 
      setIsValidToken(true);
      saveBtnStyleRef.current = "btn btn--success";
    } else {
      alert("Unauthorized. Connection Rejected.");
      setIsValidToken(false);
      saveBtnStyleRef.current = "btn";
    }
  } catch (error) {
    console.log(error);
    alert("Something went wrong.");
  }
}

export async function getLists(setData, setLoading, id) 
{
  fetch("http://localhost:8080/hubspot-lists", {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: id
  })
  .then((response) => {
    if (!response.ok) {
      throw new Error("Network response was not ok");
    }
    return response.json();
  })
  .then((data) => {
    setData(data);
    setLoading(false);
  })
  .catch((error) => {
    console.log(error.message)
    setLoading(false);
  });
}

export async function getProperties() {
  
}