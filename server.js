const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 8080;
const server = http.createServer((req, res) => {
  const filePath = path.join(__dirname, 'public', 'index.html');
  res.writeHead(200, { 'Content-Type': 'text/html' });
  fs.createReadStream(filePath).pipe(res);
});

server.listen(PORT, () => {
  console.log(`Website is running at http://localhost:${PORT}`);
  console.log(`Press Ctrl+C to stop.`);
});
