<?php

namespace App\Http\Controllers;
use Maatwebsite\Excel\Facades\Excel;
use App\Exports\UsersExport;
use Illuminate\Http\Request;

class UserExport extends Controller
{
    public function exportUsers()
    {
        return Excel::download(new UsersExport, 'users.csv');
    }
}
