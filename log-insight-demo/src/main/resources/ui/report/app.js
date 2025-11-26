// State management
let isLoading = false;

// DOM elements
const loadingState = document.getElementById('loading-state');
const errorState = document.getElementById('error-state');
const noDataState = document.getElementById('no-data-state');
const reportContent = document.getElementById('report-content');
const reportMarkdown = document.getElementById('report-markdown');
const reportTimestamp = document.getElementById('report-timestamp');
const errorMessage = document.getElementById('error-message');
const refreshBtn = document.getElementById('refresh-btn');
const retryBtn = document.getElementById('retry-btn');
const pageSizeInput = document.getElementById('pageSize');

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    // Verify all elements exist
    if (!loadingState || !errorState || !noDataState || !reportContent ||
        !reportMarkdown || !reportTimestamp || !errorMessage ||
        !refreshBtn || !retryBtn) {
        console.error('Some DOM elements are missing!');
        console.log('loadingState:', loadingState);
        console.log('errorState:', errorState);
        console.log('noDataState:', noDataState);
        console.log('reportContent:', reportContent);
        console.log('reportMarkdown:', reportMarkdown);
        console.log('reportTimestamp:', reportTimestamp);
        console.log('errorMessage:', errorMessage);
        console.log('refreshBtn:', refreshBtn);
        console.log('retryBtn:', retryBtn);
        return;
    }
    
    loadHealthReport();
    
    // Event listeners
    refreshBtn.addEventListener('click', () => loadHealthReport());
    retryBtn.addEventListener('click', () => loadHealthReport());
});

// Load health report from API
async function loadHealthReport() {
    if (isLoading) return;
    
    isLoading = true;
    showState('loading');
    
    const pageSize = pageSizeInput ? pageSizeInput.value : 50;
    console.log(`Starting to fetch health report from /health-report?pageSize=${pageSize}`);
    
    try {
        const response = await fetch(`/health-report?pageSize=${pageSize}`, {
            method: 'GET',
            headers: {
                'Accept': 'text/plain'
            }
        });
        
        console.log('Response status:', response.status);
        console.log('Response headers:', response.headers);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const reportText = await response.text();
        console.log('Report text received, length:', reportText.length);
        console.log('First 200 chars:', reportText.substring(0, 200));
        
        // Check if it's the "insufficient data" message
        if (reportText.includes('Insufficient data')) {
            console.log('Insufficient data detected');
            showState('no-data');
        } else {
            console.log('Displaying report');
            displayReport(reportText);
            showState('report');
        }
    } catch (error) {
        console.error('Error loading health report:', error);
        errorMessage.textContent = `Failed to load report: ${error.message}`;
        showState('error');
    } finally {
        isLoading = false;
    }
}

// Display the report
function displayReport(markdownText) {
    // Display as plain text (no markdown conversion)
    reportMarkdown.textContent = markdownText;
    
    // Update timestamp
    const now = new Date();
    reportTimestamp.textContent = now.toLocaleString();
}

// Simple markdown to HTML converter
function convertMarkdownToHtml(markdown) {
    let html = markdown;
    
    // Escape HTML
    html = html.replace(/&/g, '&')
               .replace(/</g, '<')
               .replace(/>/g, '>');
    
    // Headers
    html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>');
    html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>');
    html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>');
    
    // Bold
    html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    html = html.replace(/__(.*?)__/g, '<strong>$1</strong>');
    
    // Italic
    html = html.replace(/\*(.*?)\*/g, '<em>$1</em>');
    html = html.replace(/_(.*?)_/g, '<em>$1</em>');
    
    // Code blocks
    html = html.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>');
    
    // Inline code
    html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
    
    // Links
    html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>');
    
    // Unordered lists
    html = html.replace(/^\* (.*$)/gim, '<li>$1</li>');
    html = html.replace(/^- (.*$)/gim, '<li>$1</li>');
    html = html.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>');
    
    // Ordered lists
    html = html.replace(/^\d+\. (.*$)/gim, '<li>$1</li>');
    
    // Blockquotes
    html = html.replace(/^> (.*$)/gim, '<blockquote>$1</blockquote>');
    
    // Horizontal rules
    html = html.replace(/^---$/gim, '<hr>');
    html = html.replace(/^\*\*\*$/gim, '<hr>');
    
    // Line breaks and paragraphs
    html = html.split('\n\n').map(para => {
        if (para.trim() && 
            !para.startsWith('<h') && 
            !para.startsWith('<ul') && 
            !para.startsWith('<ol') && 
            !para.startsWith('<pre') && 
            !para.startsWith('<blockquote') &&
            !para.startsWith('<hr')) {
            return `<p>${para.trim()}</p>`;
        }
        return para;
    }).join('\n');
    
    // Clean up nested lists
    html = html.replace(/<\/li>\s*<li>/g, '</li><li>');
    html = html.replace(/<li>(.*?)<\/li>/gs, (match) => {
        if (!match.includes('<ul>') && !match.includes('<ol>')) {
            return match;
        }
        return match;
    });
    
    return html;
}

// Show specific state
function showState(state) {
    loadingState.style.display = 'none';
    errorState.style.display = 'none';
    noDataState.style.display = 'none';
    reportContent.style.display = 'none';
    
    switch (state) {
        case 'loading':
            loadingState.style.display = 'block';
            refreshBtn.disabled = true;
            break;
        case 'error':
            errorState.style.display = 'block';
            refreshBtn.disabled = false;
            break;
        case 'no-data':
            noDataState.style.display = 'block';
            refreshBtn.disabled = false;
            break;
        case 'report':
            reportContent.style.display = 'block';
            refreshBtn.disabled = false;
            break;
    }
}

// Made with Bob
