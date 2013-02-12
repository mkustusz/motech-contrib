'use strict';

angular.module('callLog', ['motech-dashboard', 'CalllogSearchService', 'CalllogCountService', 'ngCookies', 'bootstrap'])
    .config(['$routeProvider', function ($routeProvider) {

        $routeProvider
            .when('/logs', { templateUrl: '../callLog/resources/partials/logs.html', controller: CalllogController })
            .otherwise({redirectTo: '/logs'});
    }]);
