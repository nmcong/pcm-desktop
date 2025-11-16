/**
 * Main JavaScript file for Employee Management System
 */

$(document).ready(function () {
    console.log('Employee Management System initialized');

    // Auto-hide alerts after 5 seconds
    setTimeout(function () {
        $('.alert').fadeOut('slow', function () {
            $(this).remove();
        });
    }, 5000);

    // Initialize tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Confirm before leaving form with unsaved changes
    var formChanged = false;
    $('form input, form select, form textarea').on('change', function () {
        formChanged = true;
    });

    $('form').on('submit', function () {
        formChanged = false;
    });

    $(window).on('beforeunload', function () {
        if (formChanged) {
            return 'Bạn có thay đổi chưa được lưu. Bạn có chắc muốn rời khỏi trang?';
        }
    });

    // Format currency inputs
    $('input[type="number"][name="salary"]').on('blur', function () {
        var value = $(this).val();
        if (value) {
            $(this).val(Math.round(value));
        }
    });

    // Disable submit button after first click to prevent double submission
    $('form').on('submit', function () {
        $(this).find('button[type="submit"]').prop('disabled', true);
    });

    // Search functionality with debounce
    var searchTimeout;
    $('#searchInput').on('keyup', function () {
        clearTimeout(searchTimeout);
        var keyword = $(this).val();

        searchTimeout = setTimeout(function () {
            if (keyword.length >= 3 || keyword.length === 0) {
                // Perform search via AJAX
                performSearch(keyword);
            }
        }, 500);
    });

    // Table row click handler
    $('.table tbody tr').on('click', function (e) {
        // Don't trigger if clicking on buttons
        if ($(e.target).closest('.btn').length === 0) {
            var viewUrl = $(this).find('.btn-info').attr('href');
            if (viewUrl) {
                window.location.href = viewUrl;
            }
        }
    });

    // Highlight search terms
    function highlightSearchTerm(text, term) {
        if (!term) return text;
        var regex = new RegExp('(' + term + ')', 'gi');
        return text.replace(regex, '<mark>$1</mark>');
    }

    // Format date inputs to Vietnamese format
    $('input[type="date"]').each(function () {
        var input = $(this);
        if (input.val()) {
            // Already has a value, keep it
        } else {
            // Set max date to today for date of birth
            if (input.attr('name') === 'dateOfBirth') {
                var today = new Date().toISOString().split('T')[0];
                input.attr('max', today);
            }
        }
    });

    // Add loading spinner for long operations
    function showLoadingSpinner() {
        var spinner = '<div class="spinner-overlay"><div class="spinner-border spinner-border-lg text-light" role="status"><span class="visually-hidden">Loading...</span></div></div>';
        $('body').append(spinner);
    }

    function hideLoadingSpinner() {
        $('.spinner-overlay').remove();
    }

    // AJAX search function
    function performSearch(keyword) {
        if (!keyword) {
            // Reload page without search parameter
            window.location.href = window.location.pathname;
            return;
        }

        // Perform search
        window.location.href = window.location.pathname + '?search=' + encodeURIComponent(keyword);
    }

    // Print functionality
    window.printPage = function () {
        window.print();
    };

    // Export to CSV (simple implementation)
    window.exportToCSV = function () {
        var table = document.querySelector('table');
        if (!table) return;

        var csv = [];
        var rows = table.querySelectorAll('tr');

        for (var i = 0; i < rows.length; i++) {
            var row = [], cols = rows[i].querySelectorAll('td, th');

            for (var j = 0; j < cols.length - 1; j++) { // Skip last column (actions)
                var text = cols[j].innerText.replace(/"/g, '""');
                row.push('"' + text + '"');
            }

            csv.push(row.join(','));
        }

        // Download CSV
        var csvContent = 'data:text/csv;charset=utf-8,' + csv.join('\n');
        var encodedUri = encodeURI(csvContent);
        var link = document.createElement('a');
        link.setAttribute('href', encodedUri);
        link.setAttribute('download', 'employees.csv');
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    // Validate phone number format
    $('input[type="tel"]').on('keypress', function (e) {
        // Only allow numbers
        if (e.which < 48 || e.which > 57) {
            e.preventDefault();
        }
    });

    // Auto-uppercase first letter
    $('input[name="firstName"], input[name="lastName"]').on('blur', function () {
        var value = $(this).val();
        if (value) {
            $(this).val(value.charAt(0).toUpperCase() + value.slice(1));
        }
    });
});

// Utility function to format number as currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Utility function to format date
function formatDate(dateString) {
    var date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
}

