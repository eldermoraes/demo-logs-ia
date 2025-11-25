// WebSocket connection
let ws = null;
let isPaused = false;
let logs = [];
let totalLogs = 0;
let criticalCount = 0;
let errorCount = 0;

// DOM elements
const connectionStatus = document.getElementById('connection-status');
const totalLogsEl = document.getElementById('total-logs');
const criticalCountEl = document.getElementById('critical-count');
const errorCountEl = document.getElementById('error-count');
const logsTbody = document.getElementById('logs-tbody');
const emptyState = document.getElementById('empty-state');
const clearBtn = document.getElementById('clear-btn');
const pauseBtn = document.getElementById('pause-btn');
const autoScrollCheckbox = document.getElementById('auto-scroll');
const severityFilter = document.getElementById('severity-filter');
const tableContainer = document.querySelector('.table-container');

// Initialize WebSocket connection
function connectWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/logs/stream`;
    
    console.log('Attempting to connect to WebSocket:', wsUrl);
    
    ws = new WebSocket(wsUrl);
    
    ws.onopen = () => {
        console.log('✅ WebSocket connected successfully!');
        connectionStatus.textContent = 'Connected';
        connectionStatus.classList.remove('disconnected');
        connectionStatus.classList.add('connected');
    };
    
    ws.onmessage = (event) => {
        console.log('📨 Received message:', event.data);
        if (!isPaused) {
            try {
                const logData = JSON.parse(event.data);
                addLog(logData);
            } catch (e) {
                console.error('Failed to parse log data:', e);
            }
        }
    };
    
    ws.onerror = (error) => {
        console.error('❌ WebSocket error:', error);
        console.error('Error details:', {
            readyState: ws.readyState,
            url: wsUrl
        });
        connectionStatus.textContent = 'Error';
        connectionStatus.classList.remove('connected');
        connectionStatus.classList.add('disconnected');
    };
    
    ws.onclose = (event) => {
        console.log('🔌 WebSocket disconnected', {
            code: event.code,
            reason: event.reason,
            wasClean: event.wasClean
        });
        connectionStatus.textContent = 'Disconnected';
        connectionStatus.classList.remove('connected');
        connectionStatus.classList.add('disconnected');
        
        // Attempt to reconnect after 3 seconds
        console.log('⏳ Will attempt to reconnect in 3 seconds...');
        setTimeout(connectWebSocket, 3000);
    };
}

// Add a new log entry
function addLog(logData) {
    logs.unshift(logData); // Add to beginning of array
    totalLogs++;
    
    // Update counters
    if (logData.severity === 'CRITICAL') {
        criticalCount++;
    } else if (logData.severity === 'ERROR') {
        errorCount++;
    }
    
    updateCounters();
    renderLogs();
}

// Update counter displays
function updateCounters() {
    totalLogsEl.textContent = totalLogs;
    criticalCountEl.textContent = criticalCount;
    errorCountEl.textContent = errorCount;
}

// Render logs to table
function renderLogs() {
    const filterValue = severityFilter.value;
    const filteredLogs = filterValue === 'all' 
        ? logs 
        : logs.filter(log => log.severity === filterValue);
    
    if (filteredLogs.length === 0) {
        emptyState.classList.add('visible');
        logsTbody.innerHTML = '';
        return;
    }
    
    emptyState.classList.remove('visible');
    
    logsTbody.innerHTML = filteredLogs.map(log => `
        <tr>
            <td class="timestamp">${formatTimestamp(log.timestamp)}</td>
            <td>
                <span class="severity-badge severity-${log.severity}">
                    ${log.severity}
                </span>
            </td>
            <td class="component">${escapeHtml(log.component)}</td>
            <td class="error-type">${escapeHtml(log.errorType)}</td>
            <td class="root-cause">${escapeHtml(log.rootCauseSummary)}</td>
            <td class="suggested-action">${escapeHtml(log.suggestedAction)}</td>
        </tr>
    `).join('');
    
    // Auto-scroll to top if enabled
    if (autoScrollCheckbox.checked) {
        tableContainer.scrollTop = 0;
    }
}

// Format timestamp
function formatTimestamp(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleString('en-US', {
        month: 'short',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
    });
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Clear all logs
function clearLogs() {
    logs = [];
    totalLogs = 0;
    criticalCount = 0;
    errorCount = 0;
    updateCounters();
    renderLogs();
}

// Toggle pause
function togglePause() {
    isPaused = !isPaused;
    pauseBtn.textContent = isPaused ? 'Resume' : 'Pause';
    pauseBtn.classList.toggle('btn-primary');
    pauseBtn.classList.toggle('btn-secondary');
}

// Event listeners
clearBtn.addEventListener('click', clearLogs);
pauseBtn.addEventListener('click', togglePause);
severityFilter.addEventListener('change', renderLogs);

// Initialize
connectWebSocket();
emptyState.classList.add('visible');

