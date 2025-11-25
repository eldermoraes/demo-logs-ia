// WebSocket connection
let ws = null;
let isPaused = false;
let logs = [];
let totalLogs = 0;
let criticalCount = 0;
let errorCount = 0;
let warningCount = 0;
let infoCount = 0;

// DOM elements
const connectionStatus = document.getElementById('connection-status');
const totalLogsEl = document.getElementById('total-logs');
const criticalCountEl = document.getElementById('critical-count');
const errorCountEl = document.getElementById('error-count');
const warningCountEl = document.getElementById('warning-count');
const infoCountEl = document.getElementById('info-count');
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
    logs.push(logData); // Add to end of array (newest at bottom)
    totalLogs++;
    
    // Update counters
    if (logData.severity === 'CRITICAL') {
        criticalCount++;
    } else if (logData.severity === 'ERROR') {
        errorCount++;
    } else if (logData.severity === 'WARN') {
        warningCount++;
    } else if (logData.severity === 'INFO') {
        infoCount++;
    }
    
    updateCounters();
    renderLogs();
}

// Update counter displays
function updateCounters() {
    totalLogsEl.textContent = totalLogs;
    criticalCountEl.textContent = criticalCount;
    errorCountEl.textContent = errorCount;
    warningCountEl.textContent = warningCount;
    infoCountEl.textContent = infoCount;
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
    
    logsTbody.innerHTML = filteredLogs.map((log, index) => `
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
            <td style="text-align: center;">
                <button class="info-btn" onclick="showLogModal(${index})">i</button>
            </td>
            <td class="suggested-action">${escapeHtml(log.suggestedAction)}</td>
        </tr>
    `).join('');
    
    // Auto-scroll to bottom to follow newest logs
    if (autoScrollCheckbox.checked) {
        tableContainer.scrollTop = tableContainer.scrollHeight;
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
    warningCount = 0;
    infoCount = 0;
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

// Modal functionality
const modal = document.getElementById('log-modal');
const modalContent = document.getElementById('modal-log-content');
const modalClose = document.querySelector('.modal-close');

function showLogModal(index) {
    const filterValue = severityFilter.value;
    const filteredLogs = filterValue === 'all'
        ? logs
        : logs.filter(log => log.severity === filterValue);
    
    const log = filteredLogs[index];
    if (log && log.originalLog) {
        modalContent.textContent = log.originalLog;
        modal.classList.add('show');
    }
}

function closeModal() {
    modal.classList.remove('show');
}

// Close modal when clicking the X
modalClose.addEventListener('click', closeModal);

// Close modal when clicking outside
modal.addEventListener('click', (e) => {
    if (e.target === modal) {
        closeModal();
    }
});

// Close modal with Escape key
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && modal.classList.contains('show')) {
        closeModal();
    }
});

// Make showLogModal available globally
window.showLogModal = showLogModal;

// Column resizing functionality
let isResizing = false;
let currentColumn = null;
let startX = 0;
let startWidth = 0;

// Load saved column widths from localStorage
function loadColumnWidths() {
    const savedWidths = localStorage.getItem('columnWidths');
    if (savedWidths) {
        const widths = JSON.parse(savedWidths);
        Object.keys(widths).forEach(column => {
            const th = document.querySelector(`th[data-column="${column}"]`);
            if (th) {
                th.style.width = widths[column] + 'px';
            }
        });
    }
}

// Save column widths to localStorage
function saveColumnWidths() {
    const widths = {};
    document.querySelectorAll('th[data-column]').forEach(th => {
        const column = th.getAttribute('data-column');
        widths[column] = th.offsetWidth;
    });
    localStorage.setItem('columnWidths', JSON.stringify(widths));
}

// Initialize column resizing
function initColumnResize() {
    const resizers = document.querySelectorAll('.resizer');
    
    resizers.forEach(resizer => {
        resizer.addEventListener('mousedown', (e) => {
            e.preventDefault();
            isResizing = true;
            currentColumn = resizer.parentElement;
            startX = e.pageX;
            startWidth = currentColumn.offsetWidth;
            currentColumn.classList.add('resizing');
            document.body.style.cursor = 'col-resize';
        });
    });
    
    document.addEventListener('mousemove', (e) => {
        if (!isResizing) return;
        
        const width = startWidth + (e.pageX - startX);
        if (width > 50) { // Minimum width
            currentColumn.style.width = width + 'px';
        }
    });
    
    document.addEventListener('mouseup', () => {
        if (isResizing) {
            isResizing = false;
            if (currentColumn) {
                currentColumn.classList.remove('resizing');
                saveColumnWidths();
            }
            currentColumn = null;
            document.body.style.cursor = 'default';
        }
    });
}

// Event listeners
clearBtn.addEventListener('click', clearLogs);
pauseBtn.addEventListener('click', togglePause);
severityFilter.addEventListener('change', renderLogs);

// Initialize
loadColumnWidths();
initColumnResize();
connectWebSocket();
emptyState.classList.add('visible');

