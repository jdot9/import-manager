// Test connection to Five9 Configuration Web Services API
export async function testConnection(event, formData, saveBtnStyleRef, setIsValidToken)
{
  event.preventDefault();
  const base64Credentials = btoa(`${formData.five9Username}:${formData.five9Password}`);
  try {
    const response = await fetch("http://localhost:8080/five9-connection-test", {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(base64Credentials)
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

// Get Dialing List from Five9 Configuration Web Services API
export async function getDialingList(event, setData, setLoading, setError)
{
  //alert(sessionStorage.getItem("five9ConnectionId"));
  event.preventDefault();
  console.log("Requesting Dialing Lists from Five9 Web Services API");
  try {
    const response = await fetch("http://localhost:8080/five9-dialing-lists", {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: sessionStorage.getItem("five9ConnectionId")
    });
    const result = await response.json();
    if (result)
    {
      console.log("Five9 Dialing Lists received.");
      setData(result);
    } else {
      console.log("Could not find Five9 Dialing Lists.");
    }
  } catch (error) {
    console.log(error);
    alert("Something went wrong.");
  }
}

export async function getDialingList2(event, setData)
{
   // event.preventDefault();
    try {  
          const response = await fetch("http://localhost:8080/five9-dialing-lists", {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: sessionStorage.getItem("five9ConnectionId")
          });
        if (response.ok) {
            const result = await response.text();
            //setData(result);
            alert(result);
        } else {
            alert("Error: " + response.statusText);
        }
    } catch (error) {
        console.error("Error:" + error);
    }
}